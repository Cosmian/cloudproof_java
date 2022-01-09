package com.cosmian.rest.kmip.json;

import java.util.Objects;
import java.util.Optional;
// import java.util.logging.Logger;

public abstract class KmipChoice3<C1, C2, C3> {

    // private static final Logger logger =
    // Logger.getLogger(KmipChoice3.class.getName());

    private Optional<C1> c1 = Optional.empty();
    private Optional<C2> c2 = Optional.empty();
    private Optional<C3> c3 = Optional.empty();

    protected KmipChoice3(Optional<C1> c1, Optional<C2> c2, Optional<C3> c3) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
    }

    @SuppressWarnings("unchecked")
    public KmipChoice3(Object value) {
        Class<?>[] classes = KmipJson.type_parameters_for_super_class(this.getClass(), KmipChoice3.class);
        for (int i = 0; i < classes.length; i++) {
            final Class<?> p_class = classes[i];
            // logger.finer(() -> "Comparing " + value.getClass() + " with " + p_class);
            if (p_class.equals(value.getClass())) {
                if (i == 0) {
                    this.c1 = Optional.of((C1) value);
                } else if (i == 1) {
                    this.c2 = Optional.of((C2) value);
                } else if (i == 2) {
                    this.c3 = Optional.of((C3) value);
                } else {
                    throw new IllegalArgumentException(
                            "Value if of class: " + value.getClass() + " which is nos supported by this Choice");
                }
            }
        }
    }

    public Object get() {
        if (this.c1.isPresent()) {
            return this.c1.get();
        }
        if (this.c2.isPresent()) {
            return this.c2.get();
        }
        if (this.c3.isPresent()) {
            return this.c3.get();
        }
        throw new IllegalArgumentException("No value in this Choice");
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof KmipChoice3)) {
            return false;
        }
        KmipChoice3<?, ?, ?> kmipChoice3 = (KmipChoice3<?, ?, ?>) o;
        return Objects.equals(c1, kmipChoice3.c1) && Objects.equals(c2, kmipChoice3.c2)
                && Objects.equals(c3, kmipChoice3.c3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c1, c2, c3);
    }

}
