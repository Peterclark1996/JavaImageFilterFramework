
public class BilateralFilter extends Filter{
	
	int sigmaD = 3;
	int sigmaR = 3;
	int maskRadius;
	
	double[] sigmaArray;
	double twoSigmaRSquared;
	
	public BilateralFilter(String newName) {
		name = newName;
		maskSize = 3;
		maskRadius = (int)(maskSize-1)/2;
		
		twoSigmaRSquared = 2 * this.sigmaR * this.sigmaR;
		sigmaArray = new double[256];
		for (int i = 0; i < 256; i++) {
			sigmaArray[i] = Math.exp(-((i) / twoSigmaRSquared));
		}
	}
	
	public void setSigmaD(int newValue) {
		sigmaD = newValue;
	}
	
	public void setSigmaR(int newValue) {
		sigmaR = newValue;
	}
	
	public void setMaskSize(int newValue) {
		maskSize = newValue;
		maskRadius = (int)(maskSize-1)/2;
	}
	
	public int applyFilter(int[][] originalImage) {
		if(originalImage.length % 2 == 0 || originalImage[0].length % 2 == 0) {
			System.out.println("Grid passed has even width/height");
			return 0;
		}
		if(originalImage.length != originalImage[0].length) {
			System.out.println("Grid passed is a rectangle, not square");
			return 0;
		}
		
		int[][] kernel = new int[maskSize][maskSize];
		
		for (int x = -maskRadius; x < -maskRadius + maskSize; x++) {
			for (int y = -maskRadius; y < -maskRadius + maskSize; y++) {
				kernel[x + maskRadius][y + maskRadius] = (int) Math.exp(-((x * x + y * y) / (2 * sigmaD * sigmaD)));
			}
		}
		
		int output = 0;
		int totalMultiplier = 0;
		int multiplier;

		for(int x = 0; x < maskSize; x++){
			for(int y = 0; y < maskSize; y++){
				if (originalImage[x][y] != -1) {
					multiplier = (int) ((int)originalImage[x][y] * sigmaArray[Math.abs(originalImage[x][y] - originalImage[maskRadius][maskRadius])]);
					totalMultiplier += multiplier;
					output += (multiplier * originalImage[x][y]);
				}
			}
		}
		if(totalMultiplier == 0) {
			return 0;
		}
		return (int)Math.floor(output / totalMultiplier);
	}
}
