package com.thesmartnoob880.genindustry_bees.port;
import java.util.Optional;
import java.util.function.*;
/** Local lazy handle for retained internal inventory views; not a loader capability. */
public final class LazyOptional<T> {
    private Supplier<T> supplier;
    private T value;
    private LazyOptional(Supplier<T> supplier){this.supplier=supplier;}
    public static <T> LazyOptional<T> of(Supplier<T> supplier){return new LazyOptional<>(supplier);}
    public Optional<T> resolve(){if(value==null&&supplier!=null)value=supplier.get();return Optional.ofNullable(value);}
    public <X extends Throwable> T orElseThrow(Supplier<X> exception) throws X {return resolve().orElseThrow(exception);}
    public T orElse(T fallback){return resolve().orElse(fallback);}
    public void ifPresent(Consumer<T> action){resolve().ifPresent(action);}
    public boolean isPresent(){return resolve().isPresent();}
    public void invalidate(){supplier=null;value=null;}
}
