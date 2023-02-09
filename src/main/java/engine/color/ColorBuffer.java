package engine.color;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.system.Checks.CHECKS;

public class ColorBuffer extends CustomBuffer<ColorBuffer> implements Iterable<Color>
{
    public static @NotNull ColorBuffer create(@NotNull ColorFormat format, int capacity)
    {
        ByteBuffer container = ByteBuffer.allocateDirect(capacity * format.sizeof).order(ByteOrder.nativeOrder());
        return new ColorBuffer(format, container, capacity);
    }
    
    public static @NotNull ColorBuffer create(int capacity)
    {
        return create(ColorFormat.DEFAULT, capacity);
    }
    
    public static @NotNull ColorBuffer create(@NotNull ColorFormat format, long address, int capacity)
    {
        ByteBuffer container = MemoryUtil.memByteBuffer(address, capacity * format.sizeof);
        return new ColorBuffer(format, container, capacity);
    }
    
    public static @NotNull ColorBuffer create(long address, int capacity)
    {
        return create(ColorFormat.DEFAULT, address, capacity);
    }
    
    public static @Nullable ColorBuffer createSafe(@NotNull ColorFormat format, long address, int capacity)
    {
        return address == MemoryUtil.NULL ? null : create(format, address, capacity);
    }
    
    public static @Nullable ColorBuffer createSafe(long address, int capacity)
    {
        return createSafe(ColorFormat.DEFAULT, address, capacity);
    }
    
    public static @NotNull ColorBuffer wrap(@NotNull ColorFormat format, @NotNull ByteBuffer container)
    {
        return new ColorBuffer(format, container, container.remaining() / format.sizeof);
    }
    
    public static @NotNull ColorBuffer wrap(@NotNull ByteBuffer container)
    {
        return wrap(ColorFormat.DEFAULT, container);
    }
    
    public static @Nullable ColorBuffer wrapSafe(@NotNull ColorFormat format, @Nullable ByteBuffer container)
    {
        return container == null ? null : wrap(format, container);
    }
    
    public static @Nullable ColorBuffer wrapSafe(@Nullable ByteBuffer container)
    {
        return wrapSafe(ColorFormat.DEFAULT, container);
    }
    
    public static @NotNull ColorBuffer malloc(@NotNull ColorFormat format, int capacity)
    {
        ByteBuffer container = MemoryUtil.memAlloc(capacity * format.sizeof);
        return new ColorBuffer(format, container, capacity);
    }
    
    public static @NotNull ColorBuffer malloc(int capacity)
    {
        return malloc(ColorFormat.DEFAULT, capacity);
    }
    
    public static @NotNull ColorBuffer malloc(@NotNull ColorFormat format, int capacity, @NotNull MemoryStack stack)
    {
        ByteBuffer container = stack.malloc(1, capacity * format.sizeof);
        return new ColorBuffer(format, container, capacity);
    }
    
    public static @NotNull ColorBuffer malloc(int capacity, @NotNull MemoryStack stack)
    {
        return malloc(ColorFormat.DEFAULT, capacity, stack);
    }
    
    public static @NotNull ColorBuffer calloc(@NotNull ColorFormat format, int capacity)
    {
        ByteBuffer container = MemoryUtil.memCalloc(capacity, format.sizeof);
        return new ColorBuffer(format, container, capacity);
    }
    
    public static @NotNull ColorBuffer calloc(int capacity)
    {
        return calloc(ColorFormat.DEFAULT, capacity);
    }
    
    public static @NotNull ColorBuffer calloc(@NotNull ColorFormat format, int capacity, @NotNull MemoryStack stack)
    {
        ByteBuffer container = stack.calloc(1, capacity * format.sizeof);
        return new ColorBuffer(format, container, capacity);
    }
    
    public static @NotNull ColorBuffer calloc(int capacity, @NotNull MemoryStack stack)
    {
        return calloc(ColorFormat.DEFAULT, capacity, stack);
    }
    
    public static @NotNull ColorBuffer realloc(@NotNull ColorBuffer ptr, int capacity)
    {
        ByteBuffer newContainer = MemoryUtil.memRealloc(ptr.container, capacity * ptr.sizeof());
        return new ColorBuffer(ptr.format, newContainer, capacity);
    }
    
    // -------------------- Instance -------------------- //
    
    public final ColorFormat format;
    
    public ColorBuffer(@NotNull ColorFormat format, @Nullable ByteBuffer container, int remaining)
    {
        super(MemoryUtil.memAddressSafe(container), container, -1, 0, remaining, remaining);
        
        this.format = format;
    }
    
    @Override
    public int sizeof()
    {
        return this.format.sizeof;
    }
    
    @Override
    protected @NotNull ColorBuffer self()
    {
        return this;
    }
    
    public @NotNull ColorBuffer copy()
    {
        ColorBuffer copy = ColorBuffer.malloc(this.format, this.capacity);
        mark();
        MemoryUtil.memCopy(position(0), copy);
        reset();
        return copy;
    }
    
    public @NotNull Color get()
    {
        return get(nextGetIndex());
    }
    
    public @NotNull ColorBuffer get(@NotNull Color value)
    {
        return get(nextGetIndex(), value);
    }
    
    public @NotNull Color get(int index)
    {
        Color color = new Color();
        get(index, color);
        return color;
    }
    
    public @NotNull ColorBuffer get(int index, Color value)
    {
        if (this.container == null) return this;
        index *= this.format.sizeof;
        switch (this.format)
        {
            case RED ->
            {
                int gray = Color.toInt(this.container.get(index));
                value.r = gray;
                value.g = gray;
                value.b = gray;
                value.a = 255;
            }
            case RED_ALPHA ->
            {
                int gray = Color.toInt(this.container.get(index));
                value.r = gray;
                value.g = gray;
                value.b = gray;
                value.a = Color.toInt(this.container.get(index + 1));
            }
            case RGB ->
            {
                value.r = Color.toInt(this.container.get(index));
                value.g = Color.toInt(this.container.get(index + 1));
                value.b = Color.toInt(this.container.get(index + 2));
                value.a = 255;
            }
            case RGBA ->
            {
                value.r = Color.toInt(this.container.get(index));
                value.g = Color.toInt(this.container.get(index + 1));
                value.b = Color.toInt(this.container.get(index + 2));
                value.a = Color.toInt(this.container.get(index + 3));
            }
            default -> throw new UnsupportedOperationException("invalid format: " + format);
        }
        return this;
    }
    
    public @NotNull ColorBuffer put(@NotNull Colorc value)
    {
        return put(nextPutIndex(), value);
    }
    
    public @NotNull ColorBuffer put(int index, @NotNull Colorc value)
    {
        return put(index, value.r(), value.g(), value.b(), value.a());
    }
    
    public @NotNull ColorBuffer put(int index, int r, int g, int b, int a)
    {
        if (this.container == null) return this;
        index *= this.format.sizeof;
        switch (this.format)
        {
            case RED -> this.container.put(index, (byte) Color.toGray(r, g, b));
            case RED_ALPHA ->
            {
                this.container.put(index, (byte) Color.toGray(r, g, b));
                this.container.put(index + 1, (byte) Color.toInt(a));
            }
            case RGB ->
            {
                this.container.put(index, (byte) Color.toInt(r));
                this.container.put(index + 1, (byte) Color.toInt(g));
                this.container.put(index + 2, (byte) Color.toInt(b));
            }
            case RGBA ->
            {
                this.container.put(index, (byte) Color.toInt(r));
                this.container.put(index + 1, (byte) Color.toInt(g));
                this.container.put(index + 2, (byte) Color.toInt(b));
                this.container.put(index + 3, (byte) Color.toInt(a));
            }
            default -> throw new UnsupportedOperationException("invalid format: " + format);
        }
        return this;
    }
    
    public @NotNull ColorBuffer apply(@NotNull Consumer<Color> consumer)
    {
        return apply(nextGetIndex(), consumer);
    }
    
    public @NotNull ColorBuffer apply(int index, @NotNull Consumer<Color> consumer)
    {
        Color color = get(index);
        consumer.accept(color);
        return put(index, color);
    }
    
    @Override
    public void forEach(@NotNull Consumer<? super Color> action)
    {
        Objects.requireNonNull(action);
        Color color = new Color();
        for (int i = this.position; i < this.limit; i++)
        {
            get(i, color);
            action.accept(color);
            put(i, color);
        }
    }
    
    @Override
    public @NotNull Iterator<Color> iterator()
    {
        return new ColorBufferIterator(this.position, this.limit);
    }
    
    private class ColorBufferIterator implements Iterator<Color>
    {
        private final Color factory = new Color();
        
        private       int index;
        private final int fence;
    
        private ColorBufferIterator(int position, int limit)
        {
            this.index = position;
            this.fence = limit;
        }
        
        @Override
        public boolean hasNext()
        {
            return this.index < this.fence;
        }
        
        @Override
        public Color next()
        {
            if (CHECKS && this.fence <= this.index) throw new NoSuchElementException();
            get(this.index++, this.factory);
            return this.factory;
        }
        
        @Override
        public void forEachRemaining(Consumer<? super Color> action)
        {
            Objects.requireNonNull(action);
            int i = this.index;
            try
            {
                for (; i < this.fence; i++)
                {
                    get(i, this.factory);
                    action.accept(this.factory);
                    put(i, this.factory);
                }
            }
            finally
            {
                this.index = i;
            }
        }
    }
}
