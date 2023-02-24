package other;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BinaryPackingTest
{
    public static final class Node
    {
        boolean used = false;
        int     x, y, w, h;
        Node down, right, fit;
        
        public Node(int x, int y, int w, int h)
        {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
    
    public static final class Packer
    {
        private final Node root;
        
        public Packer(int w, int h)
        {
            this.root = new Node(0, 0, w, h);
        }
        
        public void fit(Node @NotNull ... blocks)
        {
            Node node;
            for (Node block : blocks)
            {
                node = findNode(this.root, block.w, block.h);
                if (node != null) block.fit = splitNode(node, block.w, block.h);
            }
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
        
        private @NotNull Node splitNode(@NotNull Node node, int w, int h)
        {
            node.used  = true;
            node.down  = new Node(node.x, node.y + h, node.w, node.h - h);
            node.right = new Node(node.x + w, node.y, node.w - w, h);
            return node;
        }
    }
    
    public static final class GrowingPacker
    {
        private Node root;
        
        public void fit(Node @NotNull ... blocks)
        {
            int len = blocks.length;
            
            int w = len > 0 ? blocks[0].w : 0;
            int h = len > 0 ? blocks[0].h : 0;
            this.root = new Node(0, 0, w, h);
            
            Node node;
            for (Node block : blocks)
            {
                node = findNode(this.root, block.w, block.h);
                if (node != null) {block.fit = splitNode(node, block.w, block.h);}
                else {block.fit = growNode(block.w, block.h);}
            }
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
        
        private @NotNull Node splitNode(@NotNull Node node, int w, int h)
        {
            node.used  = true;
            node.down  = new Node(node.x, node.y + h, node.w, node.h - h);
            node.right = new Node(node.x + w, node.y, node.w - w, h);
            return node;
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
            this.root = newRoot;
            
            Node node = findNode(this.root, w, h);
            if (node != null) return splitNode(node, w, h);
            return null;
        }
        
        private @Nullable Node growDown(int w, int h)
        {
            Node newRoot = new Node(0, 0, this.root.w, this.root.h + h);
            newRoot.used  = true;
            newRoot.down  = new Node(0, this.root.h, this.root.w, h);
            newRoot.right = this.root;
            this.root = newRoot;
            
            Node node = findNode(this.root, w, h);
            if (node != null) return splitNode(node, w, h);
            return null;
        }
    }
    
    public static void main(String[] args)
    {
        Node[] blocks = {
                new Node(0, 0, 500, 200),
                new Node(0, 0, 250, 200),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50),
                new Node(0, 0, 50, 50)
        };
        
        Packer packer = new Packer(500, 500);
        packer.fit(blocks);
        
        for (Node block : blocks)
        {
            System.out.printf("x=%s y=%s w=%s h=%s%n", block.fit.x, block.fit.y, block.w, block.h);
        }
        
        GrowingPacker growingPacker = new GrowingPacker();
        growingPacker.fit(blocks);
    
        System.out.printf("w=%s h=%s%n", growingPacker.root.w, growingPacker.root.h);
        for (Node block : blocks)
        {
            System.out.printf("x=%s y=%s w=%s h=%s%n", block.fit.x, block.fit.y, block.w, block.h);
        }
    }
}
