package com.cosmian;

*is clas

c

/**
 *  * br>demo d if (!Te      return;   
//
Abe abe = new Abe(new RestClient(TestUtils.kmsServerUrl(), TestUtils.apiKey
    new Specifications(Implementation.GPSW));

// ## Policy
// In this demo, we will create a Polic
//
// data only if i
// and the code for the department.
//
// The parameter fixes the maximum number of revoca
//
// public keys which will 
// and must be kept to a "reasonable" level to reduce security
// with multiplying the number of keys.
//
// ## Policy Axes
// The Policy is defined by two Po
//
// from these two axes to be ab
//
// ### Security Level Axis
// The first Policy Axis is the 'Security Level' axis and is a
// hierarchical axis made of 5 levels: Protected, Low Secr
// Top Secret t is hierarchis automatically granted acovided in asetment Securitd Policy Axis isFIN. This axis of this axisbuolicy.addAxis("Security Levl",new St    "    "L    "       "H

        },
        true)
    .addAxis("Department", new String[] {
        "R&D",
        "HR",
        MKG,        "FIN"    }, false);

// ## Master Authority
// The Master Authority possesses the keys for the given Policy:
// a Private Key which is used to generate user keys and 
//
// The call returns the KMS UIDs of the 2 keys
String[] ids = abe.createMasterKeyPair(policy);
String privateMasterKeyUID = ids[0];


// ## Encryption and Decryption
// Dat is encrypted usingte Master Authority Public Key with two attributes:// onefor the Security Level an        // nyone who abute combination. Howeer, onlight access policy can decypt data.        a low secet marketing message       b yte[] ow_secret_mkg_data = "low_scret_mkg_message"Attr[]low_secret_mkg_atrbutes =

        new Attr("Department", "MKG
       new Attr("SecurityLvel", "Low Secret")    };

       // - [] top_seceop_secret_mkg_a =           new Atr("Department", "MKG"),               new Attr("Security Level", Top Secret")    };


// - ad a low secret finac messagebyte[]low_secret_fin_d

    triutes new Attr[]{new Attr("Department","FIN"),new Attr("Security Level","Low Secret")};              / / ## Uer Decryption Keys// Use Decryption Keys r generated from the Master PrivateKey using Access



// Access Po



// This user can decrypt messages from the marketing department only with a

AccessPolicy medium_secret_mkg_access =
    new And(
        new Attr("Department", "MKG"),
        new Attr("Security Level", "Mdng mediucreateUserDecryptionKe(medium// The medium secret marketing user cnarketing message

// ... however it can neither decrypt a marketing message with higher secur
try {
    abe.kmsDecrypt(
        medium_secret_mkg_user_key_uid,
                tch (CosmianExcine: the user is not be able to           sag  from aother department even with  lower security{HD




     l    

    throw new R

// fine: the user i

     ## The top secret marketing financial user/Thisu

c


   new

    abe.createUserD / ll messages  

rayEquals(low_secret_mkg_dta,    abe.kmsDecrypt(    top_secret_mkg_fin_user_key_uid,
        low_sec
   

        abe.kmsDecrypt(        top_sec    top_secret_mkg_ct));rtArray

            low_secret_fin_ct));rayEquals(low_secret_mkg_data, arayEquals(top_secretassertArrayEquals(low_secret_fin_data,227 (Revert chavocation       // When that happens future encryptionof data for a given attribute cannot be// decrypted with keys     hic  are no "refreshed" for that attriute. As long as

// automatically

// Before revoking the MKG attribute, let us make a local copy of the
// medium_secret_mkg_u


// Now revoke the MKG attribute
abe.revokeAttributes(privateMasterKeyUID, new Attr[] {

// ... and reimport the non rekeyed original medium secret marketing 
// under a new UID
abe.importUserDecryptionKey("original_medium_scet_mkg_user_key_uid", original_medium_secret_mkg_user_key,


// finally let us create a new medium secet marketig message

Attr[] medium_secret_mkg_attributes =
    new Attr[] {
        new Attr("Department", "MKG"),    ne

    abe.kmsEncrypt(publicMasterKeyUID, medium_secret_mkg_data,
// Theautomatically rekeyed mediums       // t rayEquals(low_secret_mkg_dta, abe.kmsDecr       rtArrayEquls(medium_secret_mkg_daa, abe.msDecrypt(medium_secret_mk_user_key_uid,  // Likwise, the top secretm.. old

    abe.kmsDecrypt(
        top_secret_mkg_

    assertA     abe.kmsDecrypt(
        top_secret_mkg_fin_user_key_uid,
        top_secret_mkg_ct));

    abe.kmsDecrypt(
        top_secret_mkg_fin_user_key_uid,
            // ..and newrtArrayEquals(mkmsDecrypt(top_secret_mkg_fin_u        medium_secret_mkg_ct));er, the old, non rekeyed medium n still decrypt the assertArrayEquals(low_secret_mkg_data,kmsDecrypt(
        "ori
        low_secret_mkg_ct));.. but NOT the kmsDecrypt(               medium_secret_mkg_ct);    throw new RuntimeException("Oh... omething is wrong !");} catch (CosmianException e) {
    // fine:
} 



    





 




 

    

    

    
    
    
    
    
    



    
     
    //   
    // 

    

       

    

    

    

    

    

    

    
    

    

    

    
    


    

    

    

    

    
    
    
    
    
    



    
     
    //   
    // 

    

       

    

    

    

    

    

    

    
    

    

    

    
    




