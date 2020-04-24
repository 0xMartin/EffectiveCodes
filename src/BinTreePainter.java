import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

public class BinTreePainter extends JPanel implements MouseWheelListener {

    //barva uzlu
    private Color nodeColor = Color.GREEN;

    //barva root uzlu
    private Color rootColor = Color.RED;

    //meritko vykreslovani
    private float scale = 1.0f;

    public BinTreePainter() {
        super.setBackground(Color.WHITE);
        super.setForeground(Color.BLACK);
        super.setFont(new Font("tahoma", Font.PLAIN, 14));
        super.addMouseWheelListener(this);
    }

    /**
     * Priblizovani/Oddalovani
     * @param mouseWheelEvent MouseWheelEvent
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        if(mouseWheelEvent.getWheelRotation() != 1){
            if(this.scale < 5.0) {
                this.scale += 0.1;
            }
        }else {
            if(this.scale > 0.2) {
                this.scale -= 0.1;
            }
        }
        resize();
        super.repaint();
    }

    /**
     * Uzel stromu
     */
    private static class TreeNode {

        //potomci uzlu
        private final List<TreeNode> childs;

        //text uzlu
        private final String text;

        public TreeNode(String text){
            this.text = text;
            this.childs = new ArrayList<>();
        }

        public List<TreeNode> getChilds(){
            return this.childs;
        }

        //hloubka stromu potomku tohoto uzlu
        public int depth() {
            int max = 0;
            for(TreeNode child : this.childs) {
                max = Math.max(max, child.depth());
            }
            return max + 1;
        }

        //maximalni pocet potomku uzlku, ktery nalezi tomuto stromu
        public int maxChildCount(){
            int max = this.childs.size();
            for(TreeNode child : this.childs) {
                max = Math.max(max, child.maxChildCount());
            }
            return max;
        }

    }

    private TreeNode rootNode;

    /**
     * Vykresli binarni strom pro stupni kod
     */
    public void paintTree(CodeWord[] codeWords){
        //convert shannon nodes to tree nodes
        this.rootNode = new TreeNode("");

        //vytvori binarni strom
        buildTree(this.rootNode, codeWords, 0);

        //prizpusobi velikost kreslici plochy
        resize();

        //vyktresli binarni strom
        super.repaint();
    }

    /**
     * Sestavi binarni strom
     * @param root Root uzel binarniho stromu
     * @param codeWords Kod
     * @param depth Aktualni hloubka stromu
     */
    public void buildTree(TreeNode root, CodeWord[] codeWords, int depth){
        List<CodeWord> nodes0 = new ArrayList<>();
        List<CodeWord> nodes1 = new ArrayList<>();

        for(CodeWord codeWord : codeWords){
            if(codeWord.code.length() > depth) {
                if (codeWord.code.charAt(depth) == '0') {
                    nodes0.add(codeWord);
                } else {
                    nodes1.add(codeWord);
                }
            }
        }

        if(nodes0.size() != 0) {
            CodeWord n = nodes0.get(0);
            TreeNode child = new TreeNode(nodes0.size() == 1 ? ("'" + n.character+"':" + n.code) : "");
            root.getChilds().add(child);
            buildTree(child, nodes0.toArray(new CodeWord[0]), depth+1);
        }

        if(nodes1.size() != 0) {
            CodeWord n = nodes1.get(0);
            TreeNode child = new TreeNode(nodes1.size() == 1 ? ("'" + n.character+"':" + n.code) : "");
            root.getChilds().add(child);
            buildTree(child, nodes1.toArray(new CodeWord[0]), depth+1);
        }
    }

    @Override
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;

        super.paintComponent(g2);

        //clear all
        g2.clearRect(0, 0, super.getWidth(), super.getHeight());

        //configuration
        g2.scale(this.scale, this.scale);
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2.setStroke(new BasicStroke(2));

        //draw bin tree
        if(this.rootNode != null){
            paintNodes(
                    g2,
                    this.rootNode,
                    new Point(5, (int)(super.getHeight() / 2 / this.scale)),
                    getNodeDist()
            );
        }
    }

    private void resize() {
        if(this.rootNode != null) {
            Dimension size = getTreeSize(this.rootNode, new Point(5, (int)(super.getHeight() / 2)), this.getNodeDist());
            super.setPreferredSize(new Dimension((int)(size.width * this.scale), (int)(size.height * this.scale)));
            super.revalidate();
        }
    }

    /**
     * Vypocita velikost binarniho stromu
     * @param root Root uzel
     * @param offset Offset pro root node
     * @param dist Vzadalenost mezi root node a jeho potomky
     * @return Velikost stromu
     */
    private Dimension getTreeSize(TreeNode root, Point offset, int dist) {
        Dimension size = new Dimension(
                offset.x + root.text.length() * super.getFont().getSize(),
                Math.abs(offset.y - super.getHeight()/2 + 10)*2
        );

        int childCount = root.getChilds().size();
        for(int i = 0; i < childCount; i++){
            //offset pro dalsiho potomka
            Point nextOff = new Point(
                    offset.x + dist,
                    offset.y + (dist - dist * 2 * i/(childCount-1))
            );
            Dimension s2 = getTreeSize(root.getChilds().get(i), nextOff, dist / childCount);
            size.width = Math.max(size.width, s2.width);
            size.height = Math.max(size.height, s2.height);
        }

        return size;
    }

    /**
     * Vykresli binarni strom
     * @param g2 Graficky kontext
     * @param root Root uzlu
     * @param offset Offset pro root node
     * @param dist Vzdalenost mezi root node a jeho potomky
     */
    private void paintNodes(Graphics2D g2, TreeNode root, Point offset, int dist) {
        g2.setColor(root == this.rootNode ? this.rootColor : this.nodeColor);
        g2.fillOval(offset.x-5, offset.y-5, 10, 10);

        //vykresli vsechny potomku root uzlu
        int childCount = root.getChilds().size();
        for(int i = 0; i < childCount; i++){
            //offset pro potomky
            Point nextOff = new Point(
                    offset.x + dist,
                    offset.y + (dist - dist * 2 * i/(childCount-1))
            );
            paintNodes(g2, root.getChilds().get(i), nextOff, dist / childCount);
            //spojni uzlu a jeho potomka
            g2.setColor(getForeground());
            g2.drawLine(offset.x, offset.y, nextOff.x, nextOff.y);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString(
                    i+"",
                    (nextOff.x - offset.x - g2.getFontMetrics().stringWidth(i+""))/2 + offset.x,
                    (nextOff.y - offset.y)/2 + offset.y + (i != 0 ? -5 : g2.getFontMetrics().getHeight())
            );
        }

        //text uzlu
        g2.setColor(getForeground());
        g2.drawString(root.text, offset.x + 7, offset.y);
    }

    private final int getNodeDist(){
        return (int) (10 * Math.pow(this.rootNode.maxChildCount(), this.rootNode.depth()-1));
    }

}
