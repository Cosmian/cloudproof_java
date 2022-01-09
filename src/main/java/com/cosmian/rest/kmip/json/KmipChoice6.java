package com.cosmian.rest.kmip.json;

import java.util.Objects;
import java.util.Optional;
// import java.util.logging.Logger;

public abstract class KmipChoice6<C1, C2, C3, C4, C5, C6> {

    // private static final Logger logger =
    // Logger.getLogger(KmipChoice6.class.getName());

    private Optional<C1> c1 = Optional.empty();
    private Optional<C2> c2 = Optional.empty();
    private Optional<C3> c3 = Optional.empty();
    private Optional<C4> c4 = Optional.empty();
    private Optional<C5> c5 = Optional.empty();
    private Optional<C6> c6 = Optional.empty();

    protected KmipChoice6(Optional<C1> c1, Optional<C2> c2, Optional<C3> c3, Optional<C4> c4, Optional<C5> c5,
            Optional<C6> c6) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
        this.c5 = c5;
        this.c6 = c6;
    }

    @SuppressWarnings("unchecked")
    public KmipChoice6(Object value) {
        Class<?>[] classes = KmipJson.type_parameters_for_super_class(this.getClass(), KmipChoice6.class);
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
                } else if (i == 3) {
                    this.c4 = Optional.of((C4) value);
                } else if (i == 4) {
                    this.c5 = Optional.of((C5) value);
                } else if (i == 5) {
                    this.c6 = Optional.of((C6) value);
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
        if (this.c4.isPresent()) {
            return this.c4.get();
        }
        if (this.c5.isPresent()) {
            return this.c5.get();
        }
        if (this.c6.isPresent()) {
            return this.c6.get();
        }
        throw new IllegalArgumentException("No value in this Choice");
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof KmipChoice6)) {
            return false;
        }
        KmipChoice6<?, ?, ?, ?, ?, ?> kmipChoice = (KmipChoice6<?, ?, ?, ?, ?, ?>) o;
        return Objects.equals(c1, kmipChoice.c1) && Objects.equals(c2, kmipChoice.c2)
                && Objects.equals(c3, kmipChoice.c3) && Objects.equals(c4, kmipChoice.c4)
                && Objects.equals(c5, kmipChoice.c5) && Objects.equals(c6, kmipChoice.c6);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c1, c2, c3, c4, c5, c6);
    }

    @Override
    public String toString() {
        return "{" + get().getClass().getSimpleName() + " ='" + get() + "'}";
    }

}
