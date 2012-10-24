package uk.ac.cam.db538.dexter.gui;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jf.dexlib.Util.AccessFlags;

import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.tree.WebTree;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexField;
import uk.ac.cam.db538.dexter.dex.DexMethod;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.DockController;
import bibliothek.gui.dock.DefaultDockable;
import bibliothek.gui.dock.SplitDockStation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import lombok.val;

public class MainWindow {

	private JFrame frame;
	private DockController dockController;
	private SplitDockStation dockStation;
	
	public MainWindow() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame("Dexter");
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		dockController = new DockController();
		dockController.setRootWindow(frame);
		dockController.setTheme(new EclipseTheme());
		
		dockStation = new SplitDockStation();
		dockController.add(dockStation);
		frame.add(dockStation);
		
		openFile(new File("classes.dex"));
	}
	
	/*
	 * Renderer that changes icons for class tree, and the text of leaves
	 */
	private static class ClassTreeRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;
		
		private static final ImageIcon pkgfolderIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/packagefolder.gif"));
		private static final ImageIcon pkgIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/package.gif"));
		private static final ImageIcon clsIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/class.gif"));
		private static final ImageIcon fieldInstanceDefaultIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/field_instance_default.png"));
		private static final ImageIcon fieldInstancePublicIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/field_instance_public.png"));
		private static final ImageIcon fieldInstancePrivateIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/field_instance_private.png"));
		private static final ImageIcon fieldInstanceProtectedIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/field_instance_protected.png"));
		private static final ImageIcon fieldStaticDefaultIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/field_static_default.png"));
		private static final ImageIcon fieldStaticPublicIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/field_static_public.png"));
		private static final ImageIcon fieldStaticPrivateIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/field_static_private.png"));
		private static final ImageIcon fieldStaticProtectedIcon  = 
				new ImageIcon(ClassLoader.getSystemResource("uk/ac/cam/db538/dexter/gui/img/field_static_protected.png"));

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			 super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			 
			 val node = (DefaultMutableTreeNode) value;
			 setText(getDisplayName(node));
			 
			 switch (node.getLevel()) {
			 case 0:
				 setIcon(pkgfolderIcon);
				 break;
			 case 1:
				 setIcon(pkgIcon);
				 break;
			 case 2:
				 setIcon(clsIcon);
				 break;
			 case 4:
				 val obj = node.getUserObject();
				 if (obj instanceof DexField) {
					 val field = (DexField) obj;
					 val fieldAccess = field.getAccessFlagSet();
					 val fieldStatic = field.isStatic();
					 
					 if (fieldAccess.contains(AccessFlags.PUBLIC))
						 setIcon(fieldStatic ? fieldStaticPublicIcon : fieldInstancePublicIcon);
					 else if (fieldAccess.contains(AccessFlags.PROTECTED))
						 setIcon(fieldStatic ? fieldStaticProtectedIcon : fieldInstanceProtectedIcon);
					 else if (fieldAccess.contains(AccessFlags.PRIVATE))
						 setIcon(fieldStatic ? fieldStaticPrivateIcon : fieldInstancePrivateIcon);
					 else
						 setIcon(fieldStatic ? fieldStaticDefaultIcon : fieldInstanceDefaultIcon);
				 } else if (obj instanceof DexMethod) {
					 
				 }
			 }
			 
			 return this;
		}
	}
	
	private void openFile(File filename) {
		// load the file
		Dex dex;
		try {
			dex = new Dex(filename);
		} catch (IOException ex) {
			// TODO: handle
			return;
		}
		
		// create dockable for the file
		val dockable = new DefaultDockable();
		dockable.setTitleText(filename.getName());
		dockStation.drop(dockable);

		// create split pane
		val splitPane = new WebSplitPane();
		splitPane.setDividerLocation(300);
		dockable.add(splitPane);

		// create class tree
		val classTreeRoot = new DefaultMutableTreeNode("classes.dex");
		addClassesToTree(classTreeRoot, dex.getClasses());
		
		// create list of classes
		val classTree = new WebTree(classTreeRoot);
		classTree.setCellRenderer(new ClassTreeRenderer());
		javax.swing.ToolTipManager.sharedInstance().registerComponent(classTree);
		splitPane.setLeftComponent(new WebScrollPane(classTree));
	}
	
	private static String getDisplayName(DefaultMutableTreeNode node) {
		Object obj = node.getUserObject();
		if (obj instanceof DexClass)
			return ((DexClass) obj).getType().getShortName();
		else if (obj instanceof DexField) {
			val f = (DexField) obj;
			return f.getName() + " : " + f.getType().getPrettyName();
		} else if (obj instanceof DexMethod) {
				val f = (DexMethod) obj;
				
				val str = new StringBuilder();
				str.append(f.getName());
				str.append("(");
				
				boolean first = true;
				for (val type : f.getParameterTypes()) {
					if (first)
						first = false;
					else
						str.append(", ");
					str.append(type.getPrettyName());
				}
				
				str.append(") : ");
				str.append(f.getReturnType().getPrettyName());
				
				return str.toString();
		} else
			return obj.toString();
	}
	
	private static void insertNodeAlphabetically(DefaultMutableTreeNode parent, DefaultMutableTreeNode newChild) {
		String strNewChild = getDisplayName(newChild);
		
		int insertAt = 0;
		int parentChildren = parent.getChildCount();
		while (insertAt < parentChildren) {
			String strParentsChild = getDisplayName((DefaultMutableTreeNode) parent.getChildAt(insertAt));
			if (strNewChild.compareToIgnoreCase(strParentsChild) < 0)
				break;
			++insertAt;
		}
		parent.insert(newChild, insertAt);		
	}
	
	private static void addClassesToTree(DefaultMutableTreeNode root, List<DexClass> classes) {
		val pkgNodes = new HashMap<String, DefaultMutableTreeNode>();
		
		for (val cls : classes) {
			String pkgName = cls.getType().getPackageName();
			if (pkgName == null)
				pkgName = "(default package)";
			
			DefaultMutableTreeNode pkgNode = pkgNodes.get(pkgName);
			if (pkgNode == null) {
				pkgNode = new DefaultMutableTreeNode(pkgName);
				insertNodeAlphabetically(root, pkgNode);
				pkgNodes.put(pkgName, pkgNode);
			}
			
			val clsNode = new DefaultMutableTreeNode(cls);
			insertNodeAlphabetically(pkgNode, clsNode);
			
			if (!cls.getFields().isEmpty()) {
				val fieldsNode = new DefaultMutableTreeNode(StrFields);
				insertNodeAlphabetically(clsNode, fieldsNode);
				for (val field : cls.getFields()) {
					val fieldNode = new DefaultMutableTreeNode(field);
					insertNodeAlphabetically(fieldsNode, fieldNode);
				}
			}

			if (!cls.getMethods().isEmpty()) {
				val methodsNode = new DefaultMutableTreeNode(StrMethods);
				insertNodeAlphabetically(clsNode, methodsNode);
				for (val method : cls.getMethods()) {
					val methodNode = new DefaultMutableTreeNode(method);
					insertNodeAlphabetically(methodsNode, methodNode);
				}
			}
		}
	}
	
	private static final String StrFields = "Fields";
	private static final String StrMethods = "Methods";

	// MAIN FUNCTION
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
