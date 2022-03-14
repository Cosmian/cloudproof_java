package com.cosmian.jna;


import java.io.IOException;
import java.io.Serializable;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.FromNativeContext;

/**
 * A pointer to a local encryption cache created Rust side
 * which holds the public encryption key and the policy
 */
public class LocalEncryptionCache extends PointerType  implements Serializable {

    public LocalEncryptionCache() {
        super();
        System.out.println("Empty constructor"+" "+this.hashCode());
    }

    public LocalEncryptionCache(Pointer p) {
        super(p);
        System.out.println("constructor with pointer: "+p.toString()+" "+this.hashCode());
    }
    

    private void writeObject(java.io.ObjectOutputStream out) throws IOException{
        long value = Pointer.nativeValue(this.getPointer());
        out.writeLong(value);
        out.flush();
        System.out.println("write object with value: "+value+" "+this.hashCode());

    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        long value = in.readLong();
        this.setPointer(Pointer.createConstant(value));
        System.out.println("read object with value: "+value+" "+this.hashCode()+"  "+Pointer.nativeValue(this.getPointer()));
    }
    
    // private void readObjectNoData()    throws ObjectStreamException {}

    /** The default implementation simply creates a new instance of the class
     * and assigns its pointer field.  Override if you need different behavior,
     * such as ensuring a single {@link PointerType} instance for each unique
     * {@link Pointer} value, or instantiating a different {@link PointerType}
     * subclass.
     */
    @Override
    public Object fromNative(Object nativeValue, FromNativeContext context) {
        
        System.out.println("from Native "+Pointer.nativeValue((Pointer)nativeValue)+" "+this.hashCode());
        return super.fromNative(nativeValue, context);
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("Finalize"+" "+this.hashCode());
        super.finalize();
    }
}
