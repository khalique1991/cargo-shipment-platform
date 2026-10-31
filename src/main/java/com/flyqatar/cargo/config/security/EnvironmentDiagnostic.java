package com.flyqatar.cargo.config.security;

public class EnvironmentDiagnostic {

    public static void main(String[] args) {

        String secret = System.getenv("CARGO_SHIPMENT_CLIENT_SECRET");

        System.out.println("========== ENVIRONMENT CHECK ==========");

        System.out.println(
                "Environment variable present: " + (secret != null)
        );

        System.out.println(
                "Environment variable length: " +
                        (secret == null ? 0 : secret.length())
        );

        System.out.println("=======================================");
    }
}