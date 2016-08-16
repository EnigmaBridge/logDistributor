package com.enigmabridge.log.distributor.utils;

import com.enigmabridge.log.distributor.Utils;
import okhttp3.OkHttpClient;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Initializes additional trust roots.
 * Created by dusanklinec on 03.05.16.
 */
public class EBAdditionalTrust implements Serializable {
    public static final long serialVersionUID = 1L;
    public static final String FIELD_LETSENCRYPT = "letsencrypt";
    public static final String FIELD_SYSTEM = "system";
    public static final String FIELD_CUSTOM = "custom";

    // Flags for serialization.
    protected boolean letsEncryptFlag;
    protected boolean systemFlag;
    protected Collection<Certificate> customRoots;

    // Runtime
    protected transient TrustManager[] trustManagers;
    protected transient SSLSocketFactory sslSocketFactory;

    /**
     * Initializes trust in a default way - with letsencrypt &amp; system roots included.
     */
    public EBAdditionalTrust() {
        letsEncryptFlag = true;
        systemFlag = true;
        init(letsEncryptFlag, systemFlag, (InputStream)null);
    }

    /**
     * Initializes trust in a default way - with letsencrypt &amp; system roots included + user can add
     * own trusted roots in PEM format in the input stream.
     * @param additionalRoots input stream of additional trusted roots in PEM format, concatenated. May be null.
     */
    public EBAdditionalTrust(InputStream additionalRoots) {
        letsEncryptFlag = true;
        systemFlag = true;
        init(letsEncryptFlag, systemFlag, additionalRoots);
    }

    /**
     * As LetsEcrypt is not yet in the Java trusted roots, this class offer to build own view on trusted roots for EB
     * service.
     *
     * @param letsEncrypt if true, letsencrypt trusted roots are added to the trust object.
     * @param system if true, all system roots are added to the trust object.
     */
    public EBAdditionalTrust(boolean letsEncrypt, boolean system) {
        letsEncryptFlag = letsEncrypt;
        systemFlag = system;
        init(letsEncrypt, system, (InputStream) null);
    }

    /**
     * As LetsEcrypt is not yet in the Java trusted roots, this class offer to build own view on trusted roots for EB
     * service.
     *
     * @param letsEncrypt if true, letsencrypt trusted roots are added to the trust object.
     * @param system if true, all system roots are added to the trust object.
     * @param additionalRoots  input stream of additional trusted roots in PEM format, concatenated. May be null.
     */
    public EBAdditionalTrust(boolean letsEncrypt, boolean system, InputStream additionalRoots) {
        letsEncryptFlag = letsEncrypt;
        systemFlag = system;
        init(letsEncrypt, system, additionalRoots);
    }

    /**
     * As LetsEcrypt is not yet in the Java trusted roots, this class offer to build own view on trusted roots for EB
     * service.
     *
     * @param letsEncrypt if true, letsencrypt trusted roots are added to the trust object.
     * @param system if true, all system roots are added to the trust object.
     * @param additionalRoots  Collections of additional trusted roots in X509.
     */
    public EBAdditionalTrust(boolean letsEncrypt, boolean system, Collection<? extends Certificate> additionalRoots) {
        letsEncryptFlag = letsEncrypt;
        systemFlag = system;
        init(letsEncrypt, system, additionalRoots == null ? null : new LinkedList<Certificate>(additionalRoots));
    }

    /**
     * Constructs object from its JSON serialized version.
     * @param json
     */
    public EBAdditionalTrust(JSONObject json){
        if (customRoots != null && !customRoots.isEmpty()) {
            // TODO: implement
            throw new UnsupportedOperationException("Not implemented yet");
        }

        if (json == null) {
            throw new IllegalArgumentException("Invalid JSON format");
        }

        letsEncryptFlag = Utils.getAsBoolean(json, FIELD_LETSENCRYPT);
        systemFlag = Utils.getAsBoolean(json, FIELD_SYSTEM);
        init(letsEncryptFlag, systemFlag, (InputStream) null);
    }

    public JSONObject toJSON(JSONObject json) {
        if (customRoots != null && !customRoots.isEmpty()) {
            // TODO: implement
            throw new UnsupportedOperationException("Not implemented yet");
        }

        if (json == null){
            json = new JSONObject();
        }

        json.put(FIELD_LETSENCRYPT, letsEncryptFlag);
        json.put(FIELD_SYSTEM, systemFlag);
        return json;
    }

    public EBAdditionalTrust copy(){
        return new EBAdditionalTrust(letsEncryptFlag, systemFlag, customRoots);
    }

    protected void init(boolean letsEncrypt, boolean system, InputStream additionalRoots) {
        try {
            init(letsEncrypt, system, additionalRoots == null ? null : readCertificates(additionalRoots));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected void init(boolean letsEncrypt, boolean system, Collection<? extends Certificate> additionalRoots) {
        try {
            LinkedList<TrustManager> managers = new LinkedList<TrustManager>();
            if (letsEncrypt) {
                managers.add(trustManagerForCertificates(readCertificates(letsEncryptCertificatesInputStream())));
            }

            if (system){
                final TrustManager[] systemTrustManagers = getSystemTrustManagers();
                if (systemTrustManagers != null){
                    for(TrustManager mgr : systemTrustManagers){
                        managers.add(mgr);
                    }
                }
            }

            if (additionalRoots != null && !additionalRoots.isEmpty()){
                customRoots = new LinkedList<Certificate>(additionalRoots);
                managers.add(trustManagerForCertificates(additionalRoots));
            }

            trustManagers = managers.toArray(new TrustManager[managers.size()]);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
            sslSocketFactory = sslContext.getSocketFactory();

        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Installs this trust to the client builder.
     * @param builder
     * @return EBAdditionalTrust
     */
    public EBAdditionalTrust install(OkHttpClient.Builder builder){
        builder.sslSocketFactory(sslSocketFactory);
        return this;
    }

    /**
     * Returns an input stream containing one or more Let'sEncrypt certificate PEM files.
     */
    public static InputStream letsEncryptCertificatesInputStream() {
        // PEM files for all LetsEncrypt certificates.
        final String isrgrootx1 = "-----BEGIN CERTIFICATE-----\n" +
                "MIIFazCCA1OgAwIBAgIRAIIQz7DSQONZRGPgu2OCiwAwDQYJKoZIhvcNAQELBQAw\n" +
                "TzELMAkGA1UEBhMCVVMxKTAnBgNVBAoTIEludGVybmV0IFNlY3VyaXR5IFJlc2Vh\n" +
                "cmNoIEdyb3VwMRUwEwYDVQQDEwxJU1JHIFJvb3QgWDEwHhcNMTUwNjA0MTEwNDM4\n" +
                "WhcNMzUwNjA0MTEwNDM4WjBPMQswCQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJu\n" +
                "ZXQgU2VjdXJpdHkgUmVzZWFyY2ggR3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBY\n" +
                "MTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAK3oJHP0FDfzm54rVygc\n" +
                "h77ct984kIxuPOZXoHj3dcKi/vVqbvYATyjb3miGbESTtrFj/RQSa78f0uoxmyF+\n" +
                "0TM8ukj13Xnfs7j/EvEhmkvBioZxaUpmZmyPfjxwv60pIgbz5MDmgK7iS4+3mX6U\n" +
                "A5/TR5d8mUgjU+g4rk8Kb4Mu0UlXjIB0ttov0DiNewNwIRt18jA8+o+u3dpjq+sW\n" +
                "T8KOEUt+zwvo/7V3LvSye0rgTBIlDHCNAymg4VMk7BPZ7hm/ELNKjD+Jo2FR3qyH\n" +
                "B5T0Y3HsLuJvW5iB4YlcNHlsdu87kGJ55tukmi8mxdAQ4Q7e2RCOFvu396j3x+UC\n" +
                "B5iPNgiV5+I3lg02dZ77DnKxHZu8A/lJBdiB3QW0KtZB6awBdpUKD9jf1b0SHzUv\n" +
                "KBds0pjBqAlkd25HN7rOrFleaJ1/ctaJxQZBKT5ZPt0m9STJEadao0xAH0ahmbWn\n" +
                "OlFuhjuefXKnEgV4We0+UXgVCwOPjdAvBbI+e0ocS3MFEvzG6uBQE3xDk3SzynTn\n" +
                "jh8BCNAw1FtxNrQHusEwMFxIt4I7mKZ9YIqioymCzLq9gwQbooMDQaHWBfEbwrbw\n" +
                "qHyGO0aoSCqI3Haadr8faqU9GY/rOPNk3sgrDQoo//fb4hVC1CLQJ13hef4Y53CI\n" +
                "rU7m2Ys6xt0nUW7/vGT1M0NPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNV\n" +
                "HRMBAf8EBTADAQH/MB0GA1UdDgQWBBR5tFnme7bl5AFzgAiIyBpY9umbbjANBgkq\n" +
                "hkiG9w0BAQsFAAOCAgEAVR9YqbyyqFDQDLHYGmkgJykIrGF1XIpu+ILlaS/V9lZL\n" +
                "ubhzEFnTIZd+50xx+7LSYK05qAvqFyFWhfFQDlnrzuBZ6brJFe+GnY+EgPbk6ZGQ\n" +
                "3BebYhtF8GaV0nxvwuo77x/Py9auJ/GpsMiu/X1+mvoiBOv/2X/qkSsisRcOj/KK\n" +
                "NFtY2PwByVS5uCbMiogziUwthDyC3+6WVwW6LLv3xLfHTjuCvjHIInNzktHCgKQ5\n" +
                "ORAzI4JMPJ+GslWYHb4phowim57iaztXOoJwTdwJx4nLCgdNbOhdjsnvzqvHu7Ur\n" +
                "TkXWStAmzOVyyghqpZXjFaH3pO3JLF+l+/+sKAIuvtd7u+Nxe5AW0wdeRlN8NwdC\n" +
                "jNPElpzVmbUq4JUagEiuTDkHzsxHpFKVK7q4+63SM1N95R1NbdWhscdCb+ZAJzVc\n" +
                "oyi3B43njTOQ5yOf+1CceWxG1bQVs5ZufpsMljq4Ui0/1lvh+wjChP4kqKOJ2qxq\n" +
                "4RgqsahDYVvTH9w7jXbyLeiNdd8XM2w9U/t7y0Ff/9yi0GE44Za4rF2LN9d11TPA\n" +
                "mRGunUHBcnWEvgJBQl9nJEiU0Zsnvgc/ubhPgXRR4Xq37Z0j4r7g1SgEEzwxA57d\n" +
                "emyPxgcYxn/eR44/KJ4EBs+lVDR3veyJm+kXQ99b21/+jh5Xos1AnX5iItreGCc=\n" +
                "-----END CERTIFICATE-----";

        final String lets_encrypt_x1_cross_signed = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEqDCCA5CgAwIBAgIRAJgT9HUT5XULQ+dDHpceRL0wDQYJKoZIhvcNAQELBQAw\n" +
                "PzEkMCIGA1UEChMbRGlnaXRhbCBTaWduYXR1cmUgVHJ1c3QgQ28uMRcwFQYDVQQD\n" +
                "Ew5EU1QgUm9vdCBDQSBYMzAeFw0xNTEwMTkyMjMzMzZaFw0yMDEwMTkyMjMzMzZa\n" +
                "MEoxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MSMwIQYDVQQD\n" +
                "ExpMZXQncyBFbmNyeXB0IEF1dGhvcml0eSBYMTCCASIwDQYJKoZIhvcNAQEBBQAD\n" +
                "ggEPADCCAQoCggEBAJzTDPBa5S5Ht3JdN4OzaGMw6tc1Jhkl4b2+NfFwki+3uEtB\n" +
                "BaupnjUIWOyxKsRohwuj43Xk5vOnYnG6eYFgH9eRmp/z0HhncchpDpWRz/7mmelg\n" +
                "PEjMfspNdxIknUcbWuu57B43ABycrHunBerOSuu9QeU2mLnL/W08lmjfIypCkAyG\n" +
                "dGfIf6WauFJhFBM/ZemCh8vb+g5W9oaJ84U/l4avsNwa72sNlRZ9xCugZbKZBDZ1\n" +
                "gGusSvMbkEl4L6KWTyogJSkExnTA0DHNjzE4lRa6qDO4Q/GxH8Mwf6J5MRM9LTb4\n" +
                "4/zyM2q5OTHFr8SNDR1kFjOq+oQpttQLwNh9w5MCAwEAAaOCAZIwggGOMBIGA1Ud\n" +
                "EwEB/wQIMAYBAf8CAQAwDgYDVR0PAQH/BAQDAgGGMH8GCCsGAQUFBwEBBHMwcTAy\n" +
                "BggrBgEFBQcwAYYmaHR0cDovL2lzcmcudHJ1c3RpZC5vY3NwLmlkZW50cnVzdC5j\n" +
                "b20wOwYIKwYBBQUHMAKGL2h0dHA6Ly9hcHBzLmlkZW50cnVzdC5jb20vcm9vdHMv\n" +
                "ZHN0cm9vdGNheDMucDdjMB8GA1UdIwQYMBaAFMSnsaR7LHH62+FLkHX/xBVghYkQ\n" +
                "MFQGA1UdIARNMEswCAYGZ4EMAQIBMD8GCysGAQQBgt8TAQEBMDAwLgYIKwYBBQUH\n" +
                "AgEWImh0dHA6Ly9jcHMucm9vdC14MS5sZXRzZW5jcnlwdC5vcmcwPAYDVR0fBDUw\n" +
                "MzAxoC+gLYYraHR0cDovL2NybC5pZGVudHJ1c3QuY29tL0RTVFJPT1RDQVgzQ1JM\n" +
                "LmNybDATBgNVHR4EDDAKoQgwBoIELm1pbDAdBgNVHQ4EFgQUqEpqYwR93brm0Tm3\n" +
                "pkVl7/Oo7KEwDQYJKoZIhvcNAQELBQADggEBANHIIkus7+MJiZZQsY14cCoBG1hd\n" +
                "v0J20/FyWo5ppnfjL78S2k4s2GLRJ7iD9ZDKErndvbNFGcsW+9kKK/TnY21hp4Dd\n" +
                "ITv8S9ZYQ7oaoqs7HwhEMY9sibED4aXw09xrJZTC9zK1uIfW6t5dHQjuOWv+HHoW\n" +
                "ZnupyxpsEUlEaFb+/SCI4KCSBdAsYxAcsHYI5xxEI4LutHp6s3OT2FuO90WfdsIk\n" +
                "6q78OMSdn875bNjdBYAqxUp2/LEIHfDBkLoQz0hFJmwAbYahqKaLn73PAAm1X2kj\n" +
                "f1w8DdnkabOLGeOVcj9LQ+s67vBykx4anTjURkbqZslUEUsn2k5xeua2zUk=\n" +
                "-----END CERTIFICATE-----";

        final String lets_encrypt_x2_cross_signed = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEqDCCA5CgAwIBAgIRAMODTJjAvWslLKN5tm+lKw4wDQYJKoZIhvcNAQELBQAw\n" +
                "PzEkMCIGA1UEChMbRGlnaXRhbCBTaWduYXR1cmUgVHJ1c3QgQ28uMRcwFQYDVQQD\n" +
                "Ew5EU1QgUm9vdCBDQSBYMzAeFw0xNTEwMTkyMjM1MDFaFw0yMDEwMTkyMjM1MDFa\n" +
                "MEoxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MSMwIQYDVQQD\n" +
                "ExpMZXQncyBFbmNyeXB0IEF1dGhvcml0eSBYMjCCASIwDQYJKoZIhvcNAQEBBQAD\n" +
                "ggEPADCCAQoCggEBAOEkdEJ7t5Ex2XP/OKrYzkRctzkK3ESuDb1FuZc3Z6+9UE9f\n" +
                "0xBUa/dB2o5j5m1bwOhAqYxB/NEDif9iYQlg1gcFeJqQvRpkPk/cz3cviWvLZ69B\n" +
                "TcWNAMBr/o2E3LXylTGo6PaQoENKk3Rcsz5DaUuJIkd0UT6ZZMPNJAH5hC8odxci\n" +
                "p93DbAhMZi83dMVvk46wRjcWYdFQmMiwD09YU3ys9totlmFQrUPcCqZPnrVSuZyO\n" +
                "707fRrMx3CD8acKjIHU+7DgbNk5mZtLf9Wakky97pg6UPmA9Skscb7q0TRw8kVhu\n" +
                "L03E2nDb7QE5dsBJ5+k1tRQGkMHlkuIQ/Wu5tIUCAwEAAaOCAZIwggGOMBIGA1Ud\n" +
                "EwEB/wQIMAYBAf8CAQAwDgYDVR0PAQH/BAQDAgGGMH8GCCsGAQUFBwEBBHMwcTAy\n" +
                "BggrBgEFBQcwAYYmaHR0cDovL2lzcmcudHJ1c3RpZC5vY3NwLmlkZW50cnVzdC5j\n" +
                "b20wOwYIKwYBBQUHMAKGL2h0dHA6Ly9hcHBzLmlkZW50cnVzdC5jb20vcm9vdHMv\n" +
                "ZHN0cm9vdGNheDMucDdjMB8GA1UdIwQYMBaAFMSnsaR7LHH62+FLkHX/xBVghYkQ\n" +
                "MFQGA1UdIARNMEswCAYGZ4EMAQIBMD8GCysGAQQBgt8TAQEBMDAwLgYIKwYBBQUH\n" +
                "AgEWImh0dHA6Ly9jcHMucm9vdC14MS5sZXRzZW5jcnlwdC5vcmcwPAYDVR0fBDUw\n" +
                "MzAxoC+gLYYraHR0cDovL2NybC5pZGVudHJ1c3QuY29tL0RTVFJPT1RDQVgzQ1JM\n" +
                "LmNybDATBgNVHR4EDDAKoQgwBoIELm1pbDAdBgNVHQ4EFgQUxbGrTkyxzWQwk37B\n" +
                "hJkFq+YD4iUwDQYJKoZIhvcNAQELBQADggEBAAcSAhaE7rvHxyUnhgkEpMR56o2I\n" +
                "IH+mlw5kknjhAuvaBIAM59MZkFbFg5CrNWt8K+G3UoxJgFwv7HvJJxqwgPpNgXC/\n" +
                "uT3prkvwt+2lvzKJKbqdH+lo40P8EuSyyJOz2hjrRzNMHbJHYDS9OhF5WC5LOQQa\n" +
                "ydgLZ/JHxXgJypEZqcmVgQ+yYBs0XPwXjE7OE8vbx5REwu7gToMIqAoWRoWW2MxS\n" +
                "g28RGPVnHzHk2XV1nZGy9T+NYQ91vWWJr1pzNEFZ0cnA2xGwTeJ+zZ3URCfw3Z1U\n" +
                "+YAL3YUmrvdoRBlASOTmNJmXSo9qvMYPa3DEomAPoFQFZqsSN6kuqDEIqMA=\n" +
                "-----END CERTIFICATE-----";

        final String lets_encrypt_x3_cross_signed = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEkjCCA3qgAwIBAgIQCgFBQgAAAVOFc2oLheynCDANBgkqhkiG9w0BAQsFADA/\n" +
                "MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMT\n" +
                "DkRTVCBSb290IENBIFgzMB4XDTE2MDMxNzE2NDA0NloXDTIxMDMxNzE2NDA0Nlow\n" +
                "SjELMAkGA1UEBhMCVVMxFjAUBgNVBAoTDUxldCdzIEVuY3J5cHQxIzAhBgNVBAMT\n" +
                "GkxldCdzIEVuY3J5cHQgQXV0aG9yaXR5IFgzMIIBIjANBgkqhkiG9w0BAQEFAAOC\n" +
                "AQ8AMIIBCgKCAQEAnNMM8FrlLke3cl03g7NoYzDq1zUmGSXhvb418XCSL7e4S0EF\n" +
                "q6meNQhY7LEqxGiHC6PjdeTm86dicbp5gWAf15Gan/PQeGdxyGkOlZHP/uaZ6WA8\n" +
                "SMx+yk13EiSdRxta67nsHjcAHJyse6cF6s5K671B5TaYucv9bTyWaN8jKkKQDIZ0\n" +
                "Z8h/pZq4UmEUEz9l6YKHy9v6Dlb2honzhT+Xhq+w3Brvaw2VFn3EK6BlspkENnWA\n" +
                "a6xK8xuQSXgvopZPKiAlKQTGdMDQMc2PMTiVFrqoM7hD8bEfwzB/onkxEz0tNvjj\n" +
                "/PIzark5McWvxI0NHWQWM6r6hCm21AvA2H3DkwIDAQABo4IBfTCCAXkwEgYDVR0T\n" +
                "AQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwfwYIKwYBBQUHAQEEczBxMDIG\n" +
                "CCsGAQUFBzABhiZodHRwOi8vaXNyZy50cnVzdGlkLm9jc3AuaWRlbnRydXN0LmNv\n" +
                "bTA7BggrBgEFBQcwAoYvaHR0cDovL2FwcHMuaWRlbnRydXN0LmNvbS9yb290cy9k\n" +
                "c3Ryb290Y2F4My5wN2MwHwYDVR0jBBgwFoAUxKexpHsscfrb4UuQdf/EFWCFiRAw\n" +
                "VAYDVR0gBE0wSzAIBgZngQwBAgEwPwYLKwYBBAGC3xMBAQEwMDAuBggrBgEFBQcC\n" +
                "ARYiaHR0cDovL2Nwcy5yb290LXgxLmxldHNlbmNyeXB0Lm9yZzA8BgNVHR8ENTAz\n" +
                "MDGgL6AthitodHRwOi8vY3JsLmlkZW50cnVzdC5jb20vRFNUUk9PVENBWDNDUkwu\n" +
                "Y3JsMB0GA1UdDgQWBBSoSmpjBH3duubRObemRWXv86jsoTANBgkqhkiG9w0BAQsF\n" +
                "AAOCAQEA3TPXEfNjWDjdGBX7CVW+dla5cEilaUcne8IkCJLxWh9KEik3JHRRHGJo\n" +
                "uM2VcGfl96S8TihRzZvoroed6ti6WqEBmtzw3Wodatg+VyOeph4EYpr/1wXKtx8/\n" +
                "wApIvJSwtmVi4MFU5aMqrSDE6ea73Mj2tcMyo5jMd6jmeWUHK8so/joWUoHOUgwu\n" +
                "X4Po1QYz+3dszkDqMp4fklxBwXRsW10KXzPMTZ+sOPAveyxindmjkW8lGy+QsRlG\n" +
                "PfZ+G6Z6h7mjem0Y+iWlkYcV4PIWL1iwBi8saCbGS5jN2p8M+X+Q7UNKEkROb3N6\n" +
                "KOqkqm57TH2H3eDJAkSnh6/DNFu0Qg==\n" +
                "-----END CERTIFICATE-----";

        final String lets_encrypt_x4_cross_signed = "-----BEGIN CERTIFICATE-----\n" +
                "MIIEkjCCA3qgAwIBAgIQCgFBQgAAAVOFc6bLEeMfizANBgkqhkiG9w0BAQsFADA/\n" +
                "MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMT\n" +
                "DkRTVCBSb290IENBIFgzMB4XDTE2MDMxNzE2NDEwMloXDTIxMDMxNzE2NDEwMlow\n" +
                "SjELMAkGA1UEBhMCVVMxFjAUBgNVBAoTDUxldCdzIEVuY3J5cHQxIzAhBgNVBAMT\n" +
                "GkxldCdzIEVuY3J5cHQgQXV0aG9yaXR5IFg0MIIBIjANBgkqhkiG9w0BAQEFAAOC\n" +
                "AQ8AMIIBCgKCAQEA4SR0Qnu3kTHZc/84qtjORFy3OQrcRK4NvUW5lzdnr71QT1/T\n" +
                "EFRr90HajmPmbVvA6ECpjEH80QOJ/2JhCWDWBwV4mpC9GmQ+T9zPdy+Ja8tnr0FN\n" +
                "xY0AwGv+jYTctfKVMajo9pCgQ0qTdFyzPkNpS4kiR3RRPplkw80kAfmELyh3FyKn\n" +
                "3cNsCExmLzd0xW+TjrBGNxZh0VCYyLAPT1hTfKz22i2WYVCtQ9wKpk+etVK5nI7v\n" +
                "Tt9GszHcIPxpwqMgdT7sOBs2TmZm0t/1ZqSTL3umDpQ+YD1KSxxvurRNHDyRWG4v\n" +
                "TcTacNvtATl2wEnn6TW1FAaQweWS4hD9a7m0hQIDAQABo4IBfTCCAXkwEgYDVR0T\n" +
                "AQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwfwYIKwYBBQUHAQEEczBxMDIG\n" +
                "CCsGAQUFBzABhiZodHRwOi8vaXNyZy50cnVzdGlkLm9jc3AuaWRlbnRydXN0LmNv\n" +
                "bTA7BggrBgEFBQcwAoYvaHR0cDovL2FwcHMuaWRlbnRydXN0LmNvbS9yb290cy9k\n" +
                "c3Ryb290Y2F4My5wN2MwHwYDVR0jBBgwFoAUxKexpHsscfrb4UuQdf/EFWCFiRAw\n" +
                "VAYDVR0gBE0wSzAIBgZngQwBAgEwPwYLKwYBBAGC3xMBAQEwMDAuBggrBgEFBQcC\n" +
                "ARYiaHR0cDovL2Nwcy5yb290LXgxLmxldHNlbmNyeXB0Lm9yZzA8BgNVHR8ENTAz\n" +
                "MDGgL6AthitodHRwOi8vY3JsLmlkZW50cnVzdC5jb20vRFNUUk9PVENBWDNDUkwu\n" +
                "Y3JsMB0GA1UdDgQWBBTFsatOTLHNZDCTfsGEmQWr5gPiJTANBgkqhkiG9w0BAQsF\n" +
                "AAOCAQEANlaeSdstfAtqFN3jdRZJFjx9X+Ob3PIDlekPYQ1OQ1Uw43rE1FUj7hUw\n" +
                "g2MJKfs9b7M0WoQg7C20nJY/ajsg7pWhUG3J6rlkDTfVY9faeWi0qsPYXE6BpBDr\n" +
                "5BrW/Xv8yT8U2BiEAmNggWq8dmFl82fghmLzHBM8X8NZ3ZwA1fGePA53AP5IoD+0\n" +
                "ArpW8Ik1sSuQBjZ8oQLfN+G8OoY7MNRopyLyQQCNy4aWfE+xYnoVoa5+yr+aPiX0\n" +
                "7YQrY/cKawAn7QB4PyF5//IKSAVs7mAuB68wbMdE3FKfOHfJ24W4z/bIJTrTY8Y5\n" +
                "Sr4AUhtzf8oVDrHZYWRrP4joIcOu/Q==\n" +
                "-----END CERTIFICATE-----";

        try {
            return new ByteArrayInputStream(
                    (isrgrootx1 + "\n"
                            + lets_encrypt_x1_cross_signed + "\n"
                            + lets_encrypt_x2_cross_signed + "\n"
                            + lets_encrypt_x3_cross_signed + "\n"
                            + lets_encrypt_x4_cross_signed + "\n").getBytes("UTF-8")
            );
        } catch(UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses input stream for certificates.
     *
     * @param in input stream to parse for certificates.
     * @return collection of certificates parsed from in, possibly empty.
     * @throws GeneralSecurityException
     */
    public static Collection<? extends Certificate> readCertificates(InputStream in) throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return certificateFactory.generateCertificates(in);
    }

    /**
     * Returns a trust manager that trusts {@code certificates} and none other. HTTPS services whose
     * certificates have not been signed by these certificates will fail with a {@code
     * SSLHandshakeException}.
     *
     * <p>This can be used to replace the host platform's built-in trusted certificates with a custom
     * set. This is useful in development where certificate authority-trusted certificates aren't
     * available. Or in production, to avoid reliance on third-party certificate authorities.
     *
     * <p>See also {CertificatePinner}, which can limit trusted certificates while still using
     * the host platform's built-in trust store.
     *
     * <h3>Warning: Customizing Trusted Certificates is Dangerous!</h3>
     *
     * <p>Relying on your own trusted certificates limits your server team's ability to update their
     * TLS certificates. By installing a specific set of trusted certificates, you take on additional
     * operational complexity and limit your ability to migrate between certificate authorities. Do
     * not use custom trusted certificates in production without the blessing of your server's TLS
     * administrator.
     *
     * @param certificates
     * @return the first trust manager identified
     * @throws GeneralSecurityException if there is any security-related error
     */
    public static X509TrustManager trustManagerForCertificates(Collection<? extends Certificate> certificates)
            throws GeneralSecurityException
    {
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        char[] password = "password".toCharArray(); // Any password will work.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
        }

        return (X509TrustManager) trustManagers[0];
    }

    /**
     * Tries to load default system trust managers with system CA list.
     *
     * @return array of a system trust managers.
     * @throws NoSuchAlgorithmException - if cryptographic algorithm is unknown
     * @throws KeyStoreException - error when accessing key store
     */
    public static TrustManager[] getSystemTrustManagers() throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        // Use default platform set of trusted CAs
        tmf.init((KeyStore) null);
        TrustManager[] tms = tmf.getTrustManagers();
        if (tms == null || tms.length == 0) {
            return null;
        }

        return tms;
    }

    private static KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

            // By convention, 'null' creates an empty key store.
            keyStore.load(null, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String toString() {
        return "EBAdditionalTrust{" +
                "letsEncryptFlag=" + letsEncryptFlag +
                ", systemFlag=" + systemFlag +
                ", customRoots=" + customRoots +
                ", trustManagers=" + Arrays.toString(trustManagers) +
                ", sslSocketFactory=" + sslSocketFactory +
                '}';
    }

    public TrustManager[] getTrustManagers() {
        return trustManagers;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }
}
