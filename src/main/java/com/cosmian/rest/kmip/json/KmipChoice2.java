package com.cosmian.rest.kmip.json;

import java.util.Objects;
import java.util.Optional;
// import java.util.logging.Logger;

public abstract class KmipChoice2<C1, C2> {

    // private static final Logger logger =
    // Logger.getLogger(KmipChoice2.class.getName());

    private Optional<C1> c1 = Optional.empty();
    private Optional<C2> c2 = Optional.empty();

    protected KmipChoice2(Optional<C1> c1, Optional<C2> c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    @SuppressWarnings("unchecked")
    public KmipChoice2(Object value) {
        Class<?>[] classes = KmipJson.type_parameters_for_super_class(this.getClass(), KmipChoice2.class);
        for (int i = 0; i < classes.length; i++) {
            final Class<?> p_class = classes[i];
            // logger.finer(() -> "Comparing " + value.getClass() + " with " + p_class);
            if (p_class.equals(value.getClass())) {
                if (i == 0) {
                    this.c1 = Optional.of((C1) value);
                } else if (i == 1) {
                    this.c2 = Optional.of((C2) value);
                } else {
                    throw new IllegalArgumentException(
                        "Value of class: " + value.getClass() + " which is not supported by this Choice");
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
        throw new IllegalArgumentException("No value in this Choice");
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof KmipChoice2)) {
            return false;
        }
        KmipChoice2<?, ?> kmipChoice = (KmipChoice2<?, ?>) o;
        return Objects.equals(c1, kmipChoice.c1) && Objects.equals(c2, kmipChoice.c2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c1, c2);
    }

    @Override
    public String toString() {
        return "{" + get().getClass().getSimpleName() + " ='" + get() + "'}";
    }

}
