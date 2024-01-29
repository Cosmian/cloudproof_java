package com.cosmian.jna.covercrypt.structs;

import java.util.Arrays;

import com.cosmian.utils.CloudproofException;
import com.sun.jna.ptr.IntByReference;

public class MasterKeys extends Ffi {

    private byte[] privateKey;

    private byte[] publicKey;

    public MasterKeys(byte[] privateKey, byte[] publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }


    public void rekeyMasterKeys(String accessPolicy, Policy policy) throws CloudproofException {
        // Master Private Key
        byte[] updatedMsk = new byte[8 * 1024];
        IntByReference updatedMskSize = new IntByReference(updatedMsk.length);

        // Master Public Key OUT
        byte[] updatedMpk = new byte[8 * 1024];
        IntByReference updatedMpkSize = new IntByReference(updatedMpk.length);

        int ffiCode = INSTANCE.h_rekey_master_keys(updatedMsk, updatedMskSize,
            updatedMpk, updatedMpkSize, privateKey, privateKey.length, publicKey, publicKey.length, accessPolicy, policy.getBytes(), policy.getBytes().length);

        if (ffiCode == 1) {
            // Retry with correct allocated size
            updatedMsk = new byte[updatedMpkSize.getValue()];
            updatedMpk = new byte[updatedMpkSize.getValue()];
            INSTANCE.h_rekey_master_keys(updatedMsk, updatedMskSize,
                updatedMpk, updatedMpkSize, privateKey, privateKey.length, publicKey, publicKey.length, accessPolicy, policy.getBytes(), policy.getBytes().length);
        } else {
            unwrap(ffiCode);
        }

        this.privateKey = Arrays.copyOfRange(updatedMsk, 0, updatedMskSize.getValue());
        this.publicKey = Arrays.copyOfRange(updatedMpk, 0, updatedMpkSize.getValue());
    }


}
