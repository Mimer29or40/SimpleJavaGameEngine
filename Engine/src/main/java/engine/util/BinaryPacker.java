package engine.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BinaryPacker
{
    private static final Comparator<Container<?>> SORT_WIDTH    = Comparator.comparingInt(c -> c.width);
    private static final Comparator<Container<?>> SORT_HEIGHT   = Comparator.comparingInt(c -> c.height);
    private static final Comparator<Container<?>> SORT_MAX_SIDE = Comparator.comparingInt(c -> Math.max(c.width, c.height));
    private static final Comparator<Container<?>> SORT_AREA     = Comparator.comparingInt(c -> c.width * c.height);
    
    public Sort sortFunc = Sort.MAX_SIDE;
    
    private Node root;
    
    public <T> @NotNull Result fit(@NotNull List<Container<T>> containers)
    {
        if (this.sortFunc != null)
        {
            Comparator<Container<?>> sorter = switch (this.sortFunc)
                    {
                        case WIDTH -> SORT_WIDTH;
                        case HEIGHT -> SORT_HEIGHT;
                        case MAX_SIDE -> SORT_MAX_SIDE;
                        case AREA -> SORT_AREA;
                    };
            containers.sort(sorter.reversed());
        }
        
        int len = containers.size();
        
        int w = len > 0 ? containers.get(0).width : 0;
        int h = len > 0 ? containers.get(0).height : 0;
        this.root = new Node(0, 0, w, h);
        
        Node node;
        for (Container<T> container : containers)
        {
            node = findNode(this.root, container.width, container.height);
            if (node != null)
            {
                splitNode(node, container.width, container.height);
            }
            else
            {
                node = growNode(container.width, container.height);
            }
            if (node != null)
            {
                container.x = node.x;
                container.y = node.y;
            }
        }
        return new Result(this.root.w, this.root.h);
    }
    
    private @Nullable Node findNode(@NotNull Node root, int w, int h)
    {
        if (root.used)
        {
            Node node = findNode(root.right, w, h);
            if (node != null) return node;
            return findNode(root.down, w, h);
        }
        if (w <= root.w && h <= root.h) return root;
        return null;
    }
    
    private void splitNode(@NotNull Node node, int w, int h)
    {
        node.used  = true;
        node.down  = new Node(node.x, node.y + h, node.w, node.h - h);
        node.right = new Node(node.x + w, node.y, node.w - w, h);
    }
    
    private @Nullable Node growNode(int w, int h)
    {
        boolean canGrowDown  = w <= this.root.w;
        boolean canGrowRight = h <= this.root.h;
        
        var shouldGrowRight = canGrowRight && (this.root.h >= this.root.w + w); // attempt to keep square-ish by growing right when height is much greater than width
        var shouldGrowDown  = canGrowDown && (this.root.w >= this.root.h + h); // attempt to keep square-ish by growing down  when width  is much greater than height
        
        if (shouldGrowRight) return growRight(w, h);
        if (shouldGrowDown) return growDown(w, h);
        if (canGrowRight) return growRight(w, h);
        if (canGrowDown) return growDown(w, h);
        return null; // need to ensure sensible root starting size to avoid this happening
    }
    
    private @Nullable Node growRight(int w, int h)
    {
        Node newRoot = new Node(0, 0, this.root.w + w, this.root.h);
        newRoot.used  = true;
        newRoot.down  = this.root;
        newRoot.right = new Node(this.root.w, 0, w, this.root.h);
        this.root     = newRoot;
        
        Node node = findNode(this.root, w, h);
        if (node != null) splitNode(node, w, h);
        return node;
    }
    
    private @Nullable Node growDown(int w, int h)
    {
        Node newRoot = new Node(0, 0, this.root.w, this.root.h + h);
        newRoot.used  = true;
        newRoot.down  = new Node(0, this.root.h, this.root.w, h);
        newRoot.right = this.root;
        this.root     = newRoot;
        
        Node node = findNode(this.root, w, h);
        if (node != null) splitNode(node, w, h);
        return node;
    }
    
    public static final class Container<T>
    {
        private final T   object;
        private       int x, y;
        private final int width, height;
        
        public Container(@NotNull T object, int width, int height)
        {
            this.object = object;
            this.width  = width;
            this.height = height;
        }
        
        public @NotNull T object()
        {
            return object;
        }
        
        public int x()
        {
            return this.x;
        }
        
        public int y()
        {
            return this.y;
        }
        
        public int width()
        {
            return this.width;
        }
        
        public int height()
        {
            return this.height;
        }
    }
    
    public record Result(int width, int height) {}
    
    private static final class Node
    {
        int x, y, w, h;
        boolean used = false;
        Node    down, right;
        
        Node(int x, int y, int w, int h)
        {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
    
    public enum Sort
    {
        WIDTH,
        HEIGHT,
        MAX_SIDE,
        AREA
    }
}
