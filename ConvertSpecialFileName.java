import java.util.Scanner;

public class ConvertSpecialFileName {

	public static void main(String[] args) {
		try {
			System.out.println("START");
			Scanner scanner = new Scanner(System.in);
			
			System.out.println("Please Enter File Name...");
			String inputFileName = scanner.nextLine();

			ConvertSpecialFileName obj = new ConvertSpecialFileName();
			String outputFileName = obj.changeFileName(inputFileName);
			System.out.println(outputFileName);
			
			scanner.close();
			System.out.println("END");

		} catch (Exception e) {
			System.out.println("Exception is: " + e.getMessage());
		}
	}

	private String changeFileName(String inputFileName) throws Exception {
		if (inputFileName == null || inputFileName.length() == 0) {
			throw new Exception("Please provide valid File Name");
		}

		if (inputFileName.startsWith("DEV")) {
			return processDevFile(inputFileName);
		} else if (inputFileName.startsWith("PRD")) {
			return processProdFile(inputFileName);
		}
		return "";
	}

	private String processDevFile(String inputFileName) throws Exception {

		String cfName = getCFName(inputFileName);
		String fnum = cfName.substring(3);

		if (fnum != null && fnum.length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("CD.DML.R1Z1SYMC.IM");
			sb.append(fnum);
			sb.append("(+1)");
			return sb.toString();
		} else {
			throw new Exception("DEV: Unable to retrieve FNUM value from file name");
		}
	}

	private String processProdFile(String inputFileName) throws Exception {

		String cfName = getCFName(inputFileName);
		String fnum = cfName.substring(3);

		if (fnum != null && fnum.length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("CD.DML.R1C1SYMC.IM");
			sb.append(fnum);
			sb.append("(+1)");
			return sb.toString();
		} else {
			throw new Exception("PRD: Unable to retrieve FNUM value from file name");
		}
	}

	private String getCFName(String inputFileName) throws Exception {

		String cfName = inputFileName;

		for (int i = 0; i < 4; i++) {
			int firstIndex = cfName.indexOf('.');
			if (firstIndex > -1) {
				cfName = cfName.substring(firstIndex + 1);
			} else {
				throw new Exception("Unable to parse CFNAME from input file name");
			}
		}

		if (cfName.indexOf('.') > -1) {
			cfName = cfName.substring(0, cfName.indexOf('.'));
		} else {
			throw new Exception("Unable to get CFNAME from input file name");
		}

		if (cfName == null || cfName.length() < 4) {
			throw new Exception("CFNAME length should be atleast 4 characters");
		}

		return cfName;
	}

}
