package com.cosmian.jna.findex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.cosmian.utils.CloudproofException;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;

public final class RestToken extends FindexBase {

    /**
     * Generate a new authentication token using the given callback seeds.
     *
     * @param indexId a {@link String} used to target the correct index in the server
     * @param fetchEntrySeed the seed used to authenticate the fetch callback to the Entry Table
     * @param fetchChainSeed the seed used to authenticate the fetch callback to the Chain Table
     * @param upsertEntrySeed the seed used to authenticate the upsert callback to the Entry Table
     * @param insertChainSeed the seed used to authenticate the insert callback to the Chain Table
     * @return a {@link String} representing the token
     * @throws CloudproofException if anything goes wrong
     */
    public static String generateNewToken(String indexId,
                                          byte[] fetchEntrySeed,
                                          byte[] fetchChainSeed,
                                          byte[] upsertEntrySeed,
                                          byte[] insertChainSeed)
        throws CloudproofException {

        final Memory fetchEntrySeedPointer = new Memory(fetchEntrySeed.length);
        fetchEntrySeedPointer.write(0, fetchEntrySeed, 0, fetchEntrySeed.length);
        final Memory fetchChainSeedPointer = new Memory(fetchChainSeed.length);
        fetchChainSeedPointer.write(0, fetchChainSeed, 0, fetchChainSeed.length);
        final Memory upsertEntrySeedPointer = new Memory(upsertEntrySeed.length);
        upsertEntrySeedPointer.write(0, upsertEntrySeed, 0, upsertEntrySeed.length);
        final Memory insertChainSeedPointer = new Memory(insertChainSeed.length);
        insertChainSeedPointer.write(0, insertChainSeed, 0, insertChainSeed.length);

        byte[] tokenBuffer = new byte[200];
        IntByReference tokenBufferSize = new IntByReference(tokenBuffer.length);

        INSTANCE.h_generate_new_token(tokenBuffer, tokenBufferSize,
            indexId, fetchEntrySeedPointer,
            fetchEntrySeed.length, fetchChainSeedPointer,
            fetchChainSeed.length, upsertEntrySeedPointer,
            upsertEntrySeed.length, insertChainSeedPointer,
            insertChainSeed.length);

        byte[] tokenBytes = Arrays.copyOfRange(tokenBuffer, 0, tokenBufferSize.getValue());
        String token = new String(tokenBytes, StandardCharsets.UTF_8);

        return token;
    }
}
