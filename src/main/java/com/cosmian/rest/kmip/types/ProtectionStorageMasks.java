package com.cosmian.rest.kmip.types;

public class ProtectionStorageMasks {

    public final static int Software = 0x0000_0001;
    public final static int Hardware = 0x0000_0002;
    public final static int OnProcessor = 0x0000_0004;
    public final static int OnSystem = 0x0000_0008;
    public final static int OffSystem = 0x0000_0010;
    public final static int Hypervisor = 0x0000_0020;
    public final static int OperatingSystem = 0x0000_0040;
    public final static int Container = 0x0000_0080;
    public final static int OnPremises = 0x0000_0100;
    public final static int OffPremises = 0x0000_0200;
    public final static int SelfManaged = 0x0000_0400;
    public final static int Outsourced = 0x0000_0800;
    public final static int Validated = 0x0000_1000;
    public final static int SameJurisdiction = 0x0000_2000;

}
