package original;
import java.awt.*;
import java.io.*;import java.net.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.*;import java.util.*;
public class SketchpadClient extends JFrame implements ActionListener, ItemListener {
	Socket sock;BufferedReader Receive;
	OutputStream ostream;InputStream istream;BufferedReader keyRead;
	ObjectOutputStream oos; ObjectInputStream ois;PrintWriter pwrite;Object obj;
	
	public static void main(String[] args) throws Exception {
		SketchpadClient spc = new SketchpadClient("CLIENT PAINT");	
	}
	
	static final int WIDTH = 800;
	static final int HEIGHT = 600;
	Color c = Color.black;
	Color background = Color.white;
	Color choosed;
	int upperLeftX, upperLeftY;
	int width, height;
	int x1, y1, x2, y2;
	int drawingCount =0;
	boolean fill = false; boolean erasure = false;
	boolean clear = false;
	String drawColor = new String("black");
	String drawShape = new String("line");
	JTextField color = new JTextField(10);
	JTextField shape = new JTextField(10);
	JTextField position = new JTextField("(0,0)", 10);
	ButtonGroup fillOutline = new ButtonGroup(); 
	String[] fileNames = {"open", "save"};
	String[] colorNames = { "black", "blue", "cyan", "gray", "green", "magenta", "red", "white", "yellow" };
	String[] shapeNames = { "line", "square", "rectangle", "circle", "ellipse" };

	ArrayList<DrawingObject> drObj = new ArrayList<DrawingObject>();
	
	JFileChooser fileChooser = new JFileChooser();
	JColorChooser colorchooser = new JColorChooser();

	JFrame about = new JFrame("About Sketchpad");
	JTabbedPane jtp = new JTabbedPane();
	JPanel north1 = new JPanel();
	JPanel colorpan = new JPanel(); JPanel shapepan = new JPanel();
	JPanel checkpan = new JPanel();
	JPanel north2 = new JPanel();
	JPanel groundpan = new JPanel();
	JPanel sizepan = new JPanel();
	JPanel south = new JPanel();
	CenterJPanel canvas = new CenterJPanel();
	int drcount=0;

	
	public SketchpadClient(String s) throws Exception {
		super(s);
		this.setVisible(true);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(new Dimension(screenSize.height,screenSize.width/2));
		
		setLayout(new BorderLayout());
		setBackground(Color.yellow);
		this.add(south, BorderLayout.SOUTH);
		this.add(canvas);
		
		///JTabbedPane - "Home" Tab: colorpan/shapepan/checkpan
		jtp.setPreferredSize(new Dimension(100,100));
		jtp.addTab("Home", null, north1);
		north1.setLayout(new GridLayout(1,4,5,5));
		north1.add(colorpan); north1.add(shapepan);north1.add(checkpan);
		north1.setBackground(Color.white);

		//JTabbedPane - "Extra" Tab: groundpan/sizepan
		jtp.addTab("Extra", null, north2);north2.setLayout(new GridLayout(1,2));
		north2.add(groundpan); north2.add(sizepan);
		this.add(jtp, BorderLayout.NORTH);
		
		initializeJTextFields();
		initializeMenuComponents();
		initializeTabbedPaneButtons();
		connected();	//connect Server
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				System.exit(0);
			}
		});

	}
	
	//set connection client-server
	void connected() throws Exception{
		System.out.println("This is client. Starting connection");
		Socket sock = new Socket("127.0.0.1",9802);
		ostream = sock.getOutputStream();
		
		pwrite = new PrintWriter(ostream, true);
		istream = sock.getInputStream();
		oos = new ObjectOutputStream(ostream);
		ois = new ObjectInputStream(istream);
		
		//Connection checking
		pwrite.println("Connection Completed....");
		System.out.println("Connection Completed\n");
		pwrite.flush();
		
		//receive Server DrawingObject
		while((obj = ois.readObject())!=null) {
			System.out.println("\nanother one");
			DrawingObject a = (DrawingObject) obj;
			drObj.add(a);
			canvas.repaint();
		}
		
		pwrite.close();
	}
	
	class CenterJPanel extends JPanel {
		CenterJPanel() {
			setBackground(Color.white);
			this.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent event)
				{
					upperLeftX = 0;
					upperLeftY = 0;
					width = 0;
					height = 0;
					x1 = event.getX();
					y1 = event.getY();
					Graphics g = getGraphics();
					g.drawString(".", x1, y1);
					displayMouseCoordinates(event.getX(), event.getY());
				}
				
				public void mouseReleased(MouseEvent event)
				{
					displayMouseCoordinates(event.getX(), event.getY());
					x2 = event.getX();
					y2 = event.getY();
					upperLeftX = Math.min(x1, x2);
					upperLeftY = Math.min(y1,y2);
					width = Math.abs(x1 -x2);
					height = Math.abs(y1 - y2);

					DrawingObject newObj = new DrawingObject();
					newObj.x1 = x1;newObj.x2 = x2;newObj.y1 = y1;newObj.y2 = y2;newObj.shape = drawShape;
					newObj.upperLeftX = upperLeftX;newObj.upperLeftY = upperLeftY;newObj.width = width;
					newObj.height = height;newObj.fill = fill;newObj.color = drawColor;
					
					drObj.add(newObj);
					
//					Failed Part: send Client objects to server(send completed but Server error occured)
//					try {
//						System.out.println("Sending to Client...");
//						oos.writeObject(newObj);
//						oos.flush();
//						System.out.println("Send Completed\n");
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
					
					canvas.repaint();
					
				}
			});
			this.addMouseMotionListener(new MouseMotionListener() {
				public void mouseDragged(MouseEvent event)
				{
					Graphics g = getGraphics();

					x2 = event.getX();
					y2 = event.getY();

					// erase a small rectangular area of the window
					// if erase was selected
					displayMouseCoordinates(event.getX(), event.getY());
				}

				public void mouseMoved(MouseEvent event) {
					displayMouseCoordinates(event.getX(), event.getY());
				}
			});
			
		}

		//repaint function: paint Client Drawing Object + Update Server Drawing Object
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			this.setBackground(background);
			Iterator <DrawingObject> iterator =drObj.iterator();
			
			if(clear == true) {
				drObj.removeAll(drObj);
				clear= false;
			}
			
			
			while(iterator.hasNext()) {
				DrawingObject newObj = iterator.next();
				for(int index= 0;index !=colorNames.length;index++) {
					if(newObj.color.equals(colorNames[index])){
						switch (index) {
						case 0:
							c = Color.black;g.setColor(c);
							break;
						case 1:
							c = Color.blue;g.setColor(c);
							break;
						case 2:
							c = Color.cyan;g.setColor(c);
							break;
						case 3:
							c = Color.gray;g.setColor(c);
							break;
						case 4:
							c = Color.green;g.setColor(c);
							break;
						case 5:
							c = Color.magenta;g.setColor(c);
							break;
						case 6:
							c = Color.red;g.setColor(c);
							break;
						case 7:
							c = Color.white;g.setColor(c);
							break;
						case 8:
							c = Color.yellow;g.setColor(c);
						}
					}
					if (newObj.shape.equals("line"))
						g.drawLine(newObj.x1, newObj.y1, newObj.x2, newObj.y2);
					else if (newObj.shape.equals("square") && newObj.fill)
						g.fillRect(newObj.upperLeftX, newObj.upperLeftY, newObj.width, newObj.width);
					else if (newObj.shape.equals("square") && !newObj.fill)
						g.drawRect(newObj.upperLeftX, newObj.upperLeftY, newObj.width, newObj.width);
					else if (newObj.shape.equals("rectangle") && newObj.fill)
						g.fillRect(newObj.upperLeftX, newObj.upperLeftY, newObj.width, newObj.height);
					else if (newObj.shape.equals("rectangle") && !newObj.fill)
						g.drawRect(newObj.upperLeftX, newObj.upperLeftY, newObj.width, newObj.height);
					else if (newObj.shape.equals("circle") && newObj.fill)
						g.fillOval(newObj.upperLeftX, newObj.upperLeftY, newObj.width, newObj.width);
					else if (newObj.shape.equals("circle") && !newObj.fill)
						g.drawOval(newObj.upperLeftX, newObj.upperLeftY, newObj.width, newObj.width);
					else if (newObj.shape.equals("ellipse") && newObj.fill)
						g.fillOval(newObj.upperLeftX, newObj.upperLeftY, newObj.width, newObj.height);
					else if (newObj.shape.equals("ellipse") && !newObj.fill)
						g.drawOval(newObj.upperLeftX, newObj.upperLeftY, newObj.width, newObj.height);
				}
			}
		}
		
	}

	//south jtextfields settings
	private void initializeJTextFields() {
		color.setText(drawColor);
		south.add(color);
		shape.setText(drawShape);
		south.add(shape);
		south.add(position);
	}
	
	//menubar settings
	private void initializeMenuComponents() {
		JMenuBar bar = new JMenuBar();
		
		JMenu file = new JMenu("Files");
		JMenuItem fileitem1 = new JMenuItem("Open");
		JMenuItem fileitem2 = new JMenuItem("Save");
		file.add(fileitem1);file.add(fileitem2);
		fileitem1.addActionListener(this);fileitem2.addActionListener(this);
		bar.add(file);
		
		JMenu help = new JMenu("Help");
		JMenuItem help1 = new JMenuItem("Welcome");
		JMenuItem help2 = new JMenuItem("Help contents");
		help.add(help1);help.add(help2);
		help1.addActionListener(this);help2.addActionListener(this);
		bar.add(help);
		
		setJMenuBar(bar);
	}

	//JTabbedPane
	private void initializeTabbedPaneButtons() {
		//colorpan(JTabbedPane) settings
		JPanel color1 = new JPanel(); color1.setLayout(new GridLayout(2,4));
		colorpan.setLayout(new BorderLayout());
		JLabel colorlab = new JLabel("Color");
		JButton red = new JButton();	red.setBackground(Color.red);		red.addActionListener(this);
		JButton blue = new JButton();	blue.setBackground(Color.blue);		blue.addActionListener(this);
		JButton yellow = new JButton();	yellow.setBackground(Color.yellow);	yellow.addActionListener(this);
		JButton green = new JButton();	green.setBackground(Color.green);	green.addActionListener(this);
		JButton cyan = new JButton();	cyan.setBackground(Color.cyan);		cyan.addActionListener(this);
		JButton white = new JButton();	white.setBackground(Color.white);   white.addActionListener(this);
		JButton black = new JButton();	black.setBackground(Color.black);	black.addActionListener(this);
		JButton more = new JButton("More");	more.setBackground(Color.white);more.addActionListener(this);
		red.setActionCommand("red");blue.setActionCommand("blue");yellow.setActionCommand("yellow");
		green.setActionCommand("green");cyan.setActionCommand("cyan");white.setActionCommand("white");
		black.setActionCommand("black");
		more.setActionCommand("colorIcon");
		colorpan.add(colorlab, BorderLayout.SOUTH); colorpan.add(color1);
		color1.add(red); color1.add(blue); color1.add(yellow); color1.add(green);color1.add(cyan);
		color1.add(white);color1.add(black); color1.add(more);
		colorlab.setHorizontalAlignment(SwingConstants.CENTER); colorlab.setVisible(true);

		//shapepan(JTabbedPane) settings
		Toolkit toolkit = this.getToolkit();
		Image image=toolkit.createImage("square.png");
		Image image2 = toolkit.createImage("substract.png");
		Image image3 = toolkit.createImage("rectangular-shape-outline.png");
		Image image4 = toolkit.createImage("circle-outline.png");
		Image image5 = toolkit.createImage("ellipse-outline-shape-variant.png");
		shapepan.setLayout(new BorderLayout());
		JPanel shape1 = new JPanel(); shape1.setLayout(new GridLayout(1,4));
		JLabel shapelab = new JLabel("Shape");	
		JButton line = new JButton(new ImageIcon(image2));				line.addActionListener(this);
		JButton square = new JButton(new ImageIcon(image));			square.addActionListener(this);
		JButton rectangle = new JButton(new ImageIcon(image3));	rectangle.addActionListener(this);
		JButton circle = new JButton(new ImageIcon(image4));			circle.addActionListener(this);
		JButton ellipse = new JButton(new ImageIcon(image5));		ellipse.addActionListener(this);
		line.setOpaque(false);square.setOpaque(false);rectangle.setOpaque(false); circle.setOpaque(false);ellipse.setOpaque(false);
		line.setBorderPainted(false);square.setBorderPainted(false);rectangle.setBorderPainted(false);circle.setBorderPainted(false); ellipse.setBorderPainted(false);
		line.setContentAreaFilled(false);square.setContentAreaFilled(false);rectangle.setContentAreaFilled(false);circle.setContentAreaFilled(false); ellipse.setContentAreaFilled(false);
		//set actioncommand to sort actionPerformed command
		line.setActionCommand("line");square.setActionCommand("square");ellipse.setActionCommand("ellipse");
		rectangle.setActionCommand("rectangle"); circle.setActionCommand("circle");
		shapepan.add(shapelab, BorderLayout.SOUTH);shapelab.setHorizontalAlignment(SwingConstants.CENTER);
		shape1.add(line);shape1.add(square);shape1.add(rectangle);shape1.add(circle);
		shape1.add(ellipse); shapepan.add(shape1);
		
		//groundpan(JTabbedPane) settings
		groundpan.setLayout(new BorderLayout());
		JLabel groundlab = new JLabel("Set your Background Color");
		JPanel ground1 = new JPanel();	ground1.setLayout(new GridLayout(1,8));
		JButton groundred = new JButton();	groundred.setBackground(Color.red);		groundred.addActionListener(this);
		JButton groundblue = new JButton();	groundblue.setBackground(Color.blue);		groundblue.addActionListener(this);
		JButton groundyellow = new JButton();	groundyellow.setBackground(Color.yellow);	groundyellow.addActionListener(this);
		JButton groundgreen = new JButton();	groundgreen.setBackground(Color.green);	groundgreen.addActionListener(this);
		JButton groundcyan = new JButton();	groundcyan.setBackground(Color.cyan);		groundcyan.addActionListener(this);
		JButton groundwhite = new JButton();	groundwhite.setBackground(Color.white);   groundwhite.addActionListener(this);
		JButton groundblack = new JButton();	groundblack.setBackground(Color.black);	groundblack.addActionListener(this);
		
		JButton groundmore = new JButton("More");	groundmore.setBackground(Color.white);groundmore.addActionListener(this);
		//set actioncommand to sort actionPerformed command
		groundred.setActionCommand("groundred");groundblue.setActionCommand("groundblue");groundyellow.setActionCommand("groundyellow");
		groundgreen.setActionCommand("groundgreen");groundcyan.setActionCommand("groundcyan");groundwhite.setActionCommand("white");
		groundblack.setActionCommand("groundblack");groundmore.setActionCommand("groundmore");
		groundpan.add(groundlab, BorderLayout.SOUTH);groundpan.add(ground1);
		ground1.add(groundred); ground1.add(groundblue); ground1.add(groundyellow); ground1.add(groundgreen);ground1.add(groundcyan);
		ground1.add(groundwhite);ground1.add(groundblack); ground1.add(groundmore);
		groundlab.setHorizontalAlignment(SwingConstants.CENTER);
		
		//sizepan(JTabbedPane) settings | Used GridBagLayout
		JButton clear = new JButton("Clear Pad"); clear.addActionListener(this);
		clear.setBackground(Color.white);
		sizepan.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		sizepan.add(clear,gbc);
		
		JLabel checklab = new JLabel("fill & outline",10);
		JCheckBox fill = new JCheckBox("fill",false);
		JCheckBox outline = new JCheckBox("outline", true);
		fillOutline.add(fill);
		fillOutline.add(outline);
		checkpan.add(checklab);
		checkpan.add(fill);
		fill.addItemListener(this);
		checkpan.add(outline);
		outline.addItemListener(this);
	}


	public void actionPerformed(ActionEvent event)
	{
		Object source = event.getActionCommand();
		
		//Background Color Settings
		switch (source.toString()) {
			case "groundblack":
				System.out.println("Set background black");
				background = Color.black;
				break;
			case "groundblue":
				System.out.println("Set background blue");
				background = Color.blue;
				break;
			case "groundcyan":
				System.out.println("Set background cyan");
				background = Color.cyan;
				break;
			case "groundgreen":
				System.out.println("Set background green");
				background = Color.green;
				break;
			case "groundred":
				System.out.println("Set background red");
				background = Color.red;
				break;
			case "groundwhite":
				System.out.println("Set background white");
				background = Color.white;
				break;
			case "groundyellow":
				System.out.println("Set background yellow");
				background = Color.yellow;
				break;
			
		}
		
		//DrawingObject color settings
		if(source.equals("colorIcon")) {
			Color selected = colorchooser.showDialog(null, "color", Color.yellow);
			choosed=selected;
			drawColor = selected.toString();
			color.setText(drawColor);
		}
		
		//JFileChooser settings
		if(source.equals("Open")) {
			fileChooser.setVisible(true);
			int result = fileChooser.showOpenDialog(this);
			if(result == JFileChooser.CANCEL_OPTION)
				fileChooser.setVisible(false);
			
			File fileName = fileChooser.getSelectedFile();
			
			if(fileName == null || fileName.getName().equals("")) {
				JOptionPane.showMessageDialog(this,"Invalid Name", "Invalid Name", JOptionPane.ERROR_MESSAGE);
			}
			loadFile(fileName);
		}else if(source.equals("Save")){
			fileChooser.setVisible(true);
			int result = fileChooser.showOpenDialog(this);
			if(result == JFileChooser.CANCEL_OPTION) {
				fileChooser.setVisible(false);
			}
			File fileName = fileChooser.getSelectedFile();
			if((fileName == null) || (fileName.getName().contentEquals("")))
				JOptionPane.showMessageDialog(this,"Invalid Name", "Invalid Name", JOptionPane.ERROR_MESSAGE);
			saveFile(drObj, fileName);
		}
		
		
		// check for color chosen
		for (int index = 0; index != colorNames.length; index++)
			if (source.equals(colorNames[index])) {
				drawColor = colorNames[index];
				color.setText(drawColor);
				return;
			}

		// check for shape chosen
		for (int index = 0; index != shapeNames.length; index++)
			if (source.equals(shapeNames[index])) {
				drawShape = shapeNames[index];
				shape.setText(drawShape);
				return;
			}
		
		if(source.equals("Welcome")) {
			System.out.println("Have to put");
		}else if(source.equals("Help contents")) {
			System.out.println("Have to put");
		}else if(source.equals("Clear Pad")) {
			clear = true;
			canvas.repaint();
		}
	}
	
	//radiobutton settings
	public void itemStateChanged(ItemEvent event)
	{
		if (((JCheckBox)event.getSource()).getText() =="fill")
			fill = true;
		else if (((JCheckBox)event.getSource()).getText() =="outline")
			fill = false;
	}

	
	protected void displayMouseCoordinates(int X, int Y)
	{
		position.setText("[" + String.valueOf(X) + "," + String.valueOf(Y) + "]");
	}
	
	//JFileChooser settings
	public void loadFile(File filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(fis);
			drObj = (ArrayList<DrawingObject>)in.readObject();
			repaint();in.close();
		}catch(Exception e) {
			System.out.println(e);
		}
	}
	public void saveFile(ArrayList <DrawingObject> arr, File filename) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(arr);
			out.flush();out.close();
		}catch(IOException e) {
			System.out.println(e);
		}
	}
	
}
