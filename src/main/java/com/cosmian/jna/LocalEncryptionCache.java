package com.cosmian.jna;

import java.io.IOException;
// import java.io.ObjectStreamException;
import java.io.Serializable;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.PointerType;

/**
 * A pointer to a local encryption cache created Rust side
 * which holds the public encryption key and the policy
 */
public class LocalEncryptionCache extends PointerType  implements Serializable {


    // private void writeObject(java.io.ObjectOutputStream out) throws IOException{
    //     long value = Pointer.nativeValue(this.getValue());
    //     out.writeLong(value);
    //     out.flush();
    // }
    
    // private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
    //     long value = in.readLong();
    //     Pointer pointer =new Pointer(value);
    //     this.setValue(pointer);
    // }
    
    // private void readObjectNoData()    throws ObjectStreamException {}
}
