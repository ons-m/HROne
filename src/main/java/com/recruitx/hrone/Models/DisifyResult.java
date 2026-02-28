package com.recruitx.hrone.Models;

public class DisifyResult {

    private final String address;
    private final boolean formatValid;
    private final String domain;
    private final boolean disposable;
    private final boolean dnsValid;
    private final boolean mxValid;
    private final boolean apiFailed;

    public DisifyResult(String address,
                        boolean formatValid,
                        String domain,
                        boolean disposable,
                        boolean dnsValid,
                        boolean mxValid,
                        boolean apiFailed) {

        this.address = address;
        this.formatValid = formatValid;
        this.domain = domain;
        this.disposable = disposable;
        this.dnsValid = dnsValid;
        this.mxValid = mxValid;
        this.apiFailed = apiFailed;
    }

    public static DisifyResult apiFailure() {
        return new DisifyResult(null, true, null, false, true, true, true);
    }

    public boolean isFormatValid() { return formatValid; }
    public boolean isDisposable() { return disposable; }
    public boolean isDnsValid() { return dnsValid; }
    public boolean isMxValid() { return mxValid; }
    public boolean isApiFailed() { return apiFailed; }
}