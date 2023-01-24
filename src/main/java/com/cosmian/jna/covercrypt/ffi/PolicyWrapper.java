package com.cosmian.jna.covercrypt.ffi;

import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;

public interface PolicyWrapper extends Library {
    int h_policy(byte[] policyBuffer,
                 IntByReference policyBufferSize,
                 int maxAttributeCreations);

    int h_add_policy_axis(byte[] updatedPolicyBuffer,
                          IntByReference updatedPolicyBufferSize,
                          byte[] currentPolicyBuffer,
                          int currentPolicyBufferSize,
                          String axis);

    int h_rotate_attribute(byte[] updatedPolicyBuffer,
                           IntByReference updatedPolicyBufferSize,
                           byte[] currentPolicyBuffer,
                           int currentPolicyBufferSize,
                           String attribute);

    int h_access_policy_expression_to_json(byte[] jsonExpr,
                                           IntByReference jsonExprSize,
                                           String booleanExpression);
}
