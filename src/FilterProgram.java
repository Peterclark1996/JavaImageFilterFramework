import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FilterProgram{
	
	private static JFrame frame;
	private static JPanel imagePanel;
	
	private static JFileChooser jfc;
	
	private static int windowWidth = 918;
	private static int windowHeight = 800;//353;
	private static Insets insets;
	
	private static BufferedImage currentImage;
	private static Stack<BufferedImage> imageHistory;
	private static Filter[] filters;
	private static JComboBox filtersComboBox;
	private static int filterRepeats = 1;
	private static JSpinner filterRepeatSpinner;
	private static JLabel statusText;
	
	static ActionListener eventImport = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			jfc.setDialogTitle("Select an image");
			jfc.setAcceptAllFileFilterUsed(false);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "png", "jpeg", "bmp");
			jfc.addChoosableFileFilter(filter);
			
			int returnValue = jfc.showOpenDialog(null);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File selectedFile = jfc.getSelectedFile();
				try {
				    currentImage = ImageIO.read(new File(selectedFile.getAbsolutePath()));
				    updateImage();
				} catch (IOException ex) {
					
				}
			}
		}

	};
	
	static ActionListener eventExport = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(currentImage == null){
				return;
			}
			int returnValue = jfc.showSaveDialog(null);

			if (returnValue == JFileChooser.APPROVE_OPTION) {
				File selectedFile = jfc.getSelectedFile();
				try{
					ImageIO.write(currentImage, "png", selectedFile);
				} catch (IOException ex) {}
			}
		}

	};

	static ActionListener eventFilter = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			setBusyStatus(true);
			for(int i = 0; i < filterRepeats; i++) {
				applyFilter();
			}
			setBusyStatus(false);
		}

	};
	
	static ActionListener eventUndo = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(imageHistory.size() > 0){
				currentImage = imageHistory.pop();
				updateImage();
			}
		}

	};
	
	static ChangeListener spinnerChange = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
        	filterRepeats = (int) filterRepeatSpinner.getValue();
        }
    };
	
	public static void main(String[] args){
		frame = new JFrame();
		imageHistory = new Stack<BufferedImage>();
		jfc = new JFileChooser(System.getProperty("user.dir"));
		if(args.length > 0){
			if(Integer.parseInt(args[0]) >= 1 && Integer.parseInt(args[1]) > 1){
				setDimensions(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			}
		}
		setupFilters();
		setupFrame();
	}
	
	private static void setupFilters(){
		filters = new Filter[5];
		float[][] maskTemp;
		
		//Sharpen
		maskTemp = new float[3][3];
		maskTemp[0][0] = 0;
		maskTemp[0][1] = -1;
		maskTemp[0][2] = 0;
		maskTemp[1][0] = -1;
		maskTemp[1][1] = 5;
		maskTemp[1][2] = -1;
		maskTemp[2][0] = 0;
		maskTemp[2][1] = -1;
		maskTemp[2][2] = 0;
		filters[0] = new MaskFilter("Sharpen", maskTemp);
		
		//Box Blur
		maskTemp = new float[3][3];
		maskTemp[0][0] = 1;
		maskTemp[0][1] = 1;
		maskTemp[0][2] = 1;
		maskTemp[1][0] = 1;
		maskTemp[1][1] = 1;
		maskTemp[1][2] = 1;
		maskTemp[2][0] = 1;
		maskTemp[2][1] = 1;
		maskTemp[2][2] = 1;
		filters[1] = new MaskFilter("Box Blur", maskTemp, true);
		
		//Gaussian Blur
		maskTemp = new float[3][3];
		maskTemp[0][0] = 1;
		maskTemp[0][1] = 2;
		maskTemp[0][2] = 1;
		maskTemp[1][0] = 2;
		maskTemp[1][1] = 4;
		maskTemp[1][2] = 2;
		maskTemp[2][0] = 1;
		maskTemp[2][1] = 2;
		maskTemp[2][2] = 1;
		filters[2] = new MaskFilter("Gaussian Blur", maskTemp, true);
		
		//Gaussian Blur
		maskTemp = new float[3][3];
		maskTemp[0][0] = -1;
		maskTemp[0][1] = -1;
		maskTemp[0][2] = -1;
		maskTemp[1][0] = -1;
		maskTemp[1][1] = 8;
		maskTemp[1][2] = -1;
		maskTemp[2][0] = -1;
		maskTemp[2][1] = -1;
		maskTemp[2][2] = -1;
		filters[3] = new MaskFilter("Detect Edges", maskTemp);
		
		//Bilateral Filter
		filters[4] = new BilateralFilter("Bilateral Filter");
	}
	
	private static void setupFrame(){
		frame.setTitle("Filter Program");
		frame.setSize(windowWidth, windowHeight);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		insets = frame.getInsets();
		frame.setSize(insets.left + windowWidth + insets.right,insets.top + windowHeight + insets.bottom);
		
		frame.setLayout(new GridLayout(1, 2));
		
		//Body Panel
		JSplitPane bodyPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		frame.getContentPane().add(bodyPanel);
		bodyPanel.setDividerSize(0);
		
		//Options Panel
		JSplitPane optionsPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		bodyPanel.setLeftComponent(optionsPanel);
		optionsPanel.setDividerSize(0);
		
		//Buttons Panel
		JPanel buttonsPanel = new JPanel();
		optionsPanel.setTopComponent(buttonsPanel);
		buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		buttonsPanel.setLayout(new GridLayout(2, 2, 10, 10));
		
		JButton importButton = new JButton("Import Image");
		importButton.addActionListener(eventImport);
		buttonsPanel.add(importButton);
		
		JButton exportButton = new JButton("Export Image");
		exportButton.addActionListener(eventExport);
		buttonsPanel.add(exportButton);
		
		JButton filterButton = new JButton("Apply Filter");
		filterButton.addActionListener(eventFilter);
		buttonsPanel.add(filterButton);
		
		JButton undoButton = new JButton("Undo Last Filter");
		undoButton.addActionListener(eventUndo);
		buttonsPanel.add(undoButton);
		
		//Parameters Panel
		JSplitPane paramsPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		paramsPanel.setDividerSize(0);
		optionsPanel.setBottomComponent(paramsPanel);
		
		//Parameters Panel Top
		JPanel paramsPanelTop = new JPanel();
		paramsPanel.setTopComponent(paramsPanelTop);
		paramsPanelTop.setBorder(new EmptyBorder(10, 10, 10, 10));
		paramsPanelTop.setLayout(new GridLayout(3, 2, 10, 10));
		
		JLabel filterLabel = new JLabel("Filter: ");
		paramsPanelTop.add(filterLabel);
		
		String[] filterNames = new String[filters.length];
		for(int i = 0; i < filters.length; i++){
			filterNames[i] = filters[i].getName();
		}
		filtersComboBox = new JComboBox(filterNames);
		paramsPanelTop.add(filtersComboBox);
		
		JLabel repeatLabel = new JLabel("Repeat amount: ");
		paramsPanelTop.add(repeatLabel);
		
		SpinnerModel filterRepeatModel = new SpinnerNumberModel(1, 1, 100, 1);
		filterRepeatSpinner = new JSpinner(filterRepeatModel);
		filterRepeatSpinner.addChangeListener(spinnerChange);
		paramsPanelTop.add(filterRepeatSpinner);
		
		JLabel statusLabel = new JLabel("Status: ");
		paramsPanelTop.add(statusLabel);
		
		statusText = new JLabel("Idle");
		statusText.setForeground(Color.GREEN);
		paramsPanelTop.add(statusText);
		
		//Image Panel
		imagePanel = new JPanel();
		bodyPanel.setRightComponent(imagePanel);
		
		frame.setVisible(true);
		
		//Parameters Panel Bottom
		JPanel paramsPanelBot = new JPanel();
		paramsPanel.setBottomComponent(paramsPanelBot);
	}
	
	private static void setDimensions(int width, int height){
		windowWidth = width;
		windowHeight = height;
	}
	
	private static void setBusyStatus(boolean s) {
		if(s) {
			statusText.setText("Busy");
			statusText.setForeground(Color.RED);
		}else {
			statusText.setText("Idle");
			statusText.setForeground(Color.GREEN);
		}
		statusText.paintImmediately(statusText.getVisibleRect());
	}
	
	
	private static void updateImage(){
		imagePanel.getGraphics().drawImage(currentImage, 0, 0, frame);
	}
	
	private static void applyFilter(){
		imageHistory.push(currentImage);
		
		//Convert image into 2D int array
		byte[] pixels = ((DataBufferByte)currentImage.getRaster().getDataBuffer()).getData();
		int[][] imageIntArrayAlpha = new int[currentImage.getWidth()][currentImage.getHeight()];
		int[][] imageIntArrayRed = new int[currentImage.getWidth()][currentImage.getHeight()];
		int[][] imageIntArrayGreen = new int[currentImage.getWidth()][currentImage.getHeight()];
		int[][] imageIntArrayBlue = new int[currentImage.getWidth()][currentImage.getHeight()];
		int col = 0;
		int row = 0;
		if(currentImage.getAlphaRaster() != null){
			for (int pixel = 0; pixel < pixels.length; pixel += 4) {
				imageIntArrayAlpha[col][row] = ((int)pixels[pixel] & 0xff);
				imageIntArrayRed[col][row] = ((int)pixels[pixel + 3] & 0xff);
				imageIntArrayGreen[col][row] = ((int)pixels[pixel + 2] & 0xff);
				imageIntArrayBlue[col][row] = ((int)pixels[pixel + 1] & 0xff);
				col++;
	            if (col == currentImage.getWidth()) {
	               col = 0;
	               row++;
	            }
			}
		}else{
			for (int pixel = 0; pixel < pixels.length; pixel += 3) {
				imageIntArrayRed[col][row] = ((int)pixels[pixel + 2] & 0xff);
				imageIntArrayGreen[col][row] = ((int)pixels[pixel + 1] & 0xff);
				imageIntArrayBlue[col][row] = ((int)pixels[pixel] & 0xff);
				col++;
	            if (col == currentImage.getWidth()) {
	               col = 0;
	               row++;
	            }
			}
		}
		
		//Apply filter
		Filter currentFilter = filters[filtersComboBox.getSelectedIndex()];
		int offset = (int)(currentFilter.getMaskSize()-1)/2;
		int[][] gridRed = new int[currentFilter.getMaskSize()][currentFilter.getMaskSize()];
		int[][] gridGreen = new int[currentFilter.getMaskSize()][currentFilter.getMaskSize()];
		int[][] gridBlue = new int[currentFilter.getMaskSize()][currentFilter.getMaskSize()];
		int[][] outputIntArrayRed = new int[currentImage.getWidth()][currentImage.getHeight()];
		int[][] outputIntArrayGreen = new int[currentImage.getWidth()][currentImage.getHeight()];
		int[][] outputIntArrayBlue = new int[currentImage.getWidth()][currentImage.getHeight()];
		for(col = 0; col < currentImage.getWidth(); col++) {
		    for(row = 0; row < currentImage.getHeight(); row++) {
		    	for(int gridx = 0; gridx < currentFilter.getMaskSize(); gridx++){
		    		for(int gridy = 0; gridy < currentFilter.getMaskSize(); gridy++){
		    			if(0 <= col + gridx - offset && col + gridx - offset < currentImage.getWidth() && 0 <= row + gridy - offset && row + gridy - offset < currentImage.getHeight()){
		    				gridRed[gridx][gridy] = imageIntArrayRed[col + gridx - offset][row + gridy - offset];
		    				gridGreen[gridx][gridy] = imageIntArrayGreen[col + gridx - offset][row + gridy - offset];
		    				gridBlue[gridx][gridy] = imageIntArrayBlue[col + gridx - offset][row + gridy - offset];
		    			}else{
		    				gridRed[gridx][gridy] = -1;
		    				gridGreen[gridx][gridy] = -1;
		    				gridBlue[gridx][gridy] = -1;
		    			}
			    	}
		    	}
		    	outputIntArrayRed[col][row] = currentFilter.applyFilter(gridRed);
		    	outputIntArrayGreen[col][row] = currentFilter.applyFilter(gridGreen);
		    	outputIntArrayBlue[col][row] = currentFilter.applyFilter(gridBlue);
		    }
		}
		
		//Convert 2D int array to buffered image
		BufferedImage output = new BufferedImage(currentImage.getWidth(), currentImage.getHeight(), currentImage.getType());
		for(col = 0; col < output.getWidth(); col++) {
		    for(row = 0; row < output.getHeight(); row++) {
		    	output.setRGB(col, row, (int)outputIntArrayRed[col][row] << 16 | (int)outputIntArrayGreen[col][row] << 8| (int)outputIntArrayBlue[col][row]);
		    }
		}
		
		currentImage = output;
		updateImage();
	}
}
