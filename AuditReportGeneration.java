import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AuditReportGeneration {

	public static void main(String[] args) {

		try {
			System.out.println("START");
			Scanner scanner = new Scanner(System.in);
			System.out.println("Please Enter Folder path...");
			// String inputFileName = scanner.nextLine();
			String inputFolderPath = "D:\\files\\reportgeneration\\2";

			System.out.println("Please Enter Master file Folder path...");
			// String inputFileName = scanner.nextLine();
			String inputMasterFolderPath = "D:\\files\\reportgeneration\\2\\masterfiles";

			AuditReportGeneration obj = new AuditReportGeneration();
			obj.generateReports(inputFolderPath, inputMasterFolderPath);
			scanner.close();
			System.out.println("END");

		} catch (Exception e) {
			System.out.println("Exception is: " + e.getMessage());
		}
	}

	public void generateReports(String inputFolderPath, String inputMasterFolderPath) throws Exception {

		File folder = new File(inputFolderPath);
		File[] listOfFiles = folder.listFiles();

		File paramFile = null;
		File otherFile = null;

		for (File file : listOfFiles) {
			if (file.isFile()) {
				System.out.println(file.getName());
				String fileName = file.getName();

				if (fileName != null && fileName.toUpperCase().contains(".PARM")) {
					paramFile = file;
				} else {
					otherFile = file;
				}
			}
		}
		System.out.println(paramFile);
		System.out.println(otherFile);

		if (paramFile != null && otherFile != null) {
			processParamFiles(paramFile, otherFile, inputFolderPath, inputMasterFolderPath);
		} else {
			throw new Exception("No PARAM or AFP files available for processing.");
		}
	}

	private void processParamFiles(File paramFile, File otherFile, String inputFolderPath, String inputMasterFolderPath)
			throws Exception {

		List<String> reports = new ArrayList<String>();

		Map<String, String> fields = readParamFile(paramFile);
		String afpFileName = otherFile.getName();

		String auditFileName = getAuditContent(fields, afpFileName, reports);
		String content = reports.get(0);

		File folder = new File(inputMasterFolderPath);
		File[] listOfFiles = folder.listFiles();
		String updatedContent = content;

		if (listOfFiles != null && listOfFiles.length > 0) {
			File masterFileObj = listOfFiles[0];
			updatedContent = updateMasterFile(masterFileObj, content);
		} else {
			String masterFileName = prepareMasterFileName(fields, afpFileName);
			addHeaderForMasterFile(reports, masterFileName);
			createMasterFile(reports, inputMasterFolderPath, masterFileName);
		}
		writeAuditStringtoFile(auditFileName, updatedContent, inputFolderPath);
	}

	private String updateMasterFile(File masterFileObj, String content) throws Exception {

		List<String> reports = new ArrayList<String>();

		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(masterFileObj);
			br = new BufferedReader(fr);
			String line;

			while ((line = br.readLine()) != null) {
				reports.add(line);
			}
		} catch (Exception e) {
			System.out.println("Error while writing content to master audit file is:" + e.getMessage());
		} finally {
			closeStream(br);
			closeStream(fr);
		}

		if (reports.isEmpty()) {
			return "";
		}

		validateCycleAndJdate(reports.get(1), content);

		String header = reports.get(0);
		String fcCountVal = header.substring(0, header.indexOf(" "));
		String part = header.substring(header.indexOf(" "));

		int updatedVal = Integer.parseInt(fcCountVal) + 1;
		String updatedValStr = "";

		if (updatedVal < 10) {
			updatedValStr += "0";
		}
		updatedValStr += "" + updatedVal;
		reports.set(0, updatedValStr + part);

		String updatedContent = updatedValStr + content.substring(content.indexOf(" "));
		reports.add(updatedContent);

		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(masterFileObj);
			bw = new BufferedWriter(fw);

			for (String auditreport : reports) {
				bw.write(auditreport);
				bw.newLine();
			}
		} catch (Exception e) {
			System.out.println("Error while writing content to master audit file is:" + e.getMessage());
		} finally {
			closeStream(bw);
			closeStream(fw);
		}
		return updatedContent;
	}

	private void validateCycleAndJdate(String oldContent, String content) throws Exception {

		String[] contentParts = content.split(" ");
		String contentSbfFilename = contentParts[2];

		String[] contentSbfFilenameParts = contentSbfFilename.split("\\.");
		String newCycleVal = contentSbfFilenameParts[2];
		newCycleVal = newCycleVal.substring(newCycleVal.indexOf('R') + 1);
		String newJDate = contentSbfFilenameParts[4];

		String[] oldContentParts = oldContent.split(" ");
		String oldContentSbfFilename = oldContentParts[2];

		String[] oldContentSbfFilenameParts = oldContentSbfFilename.split("\\.");
		String oldCycleVal = oldContentSbfFilenameParts[2];
		oldCycleVal = oldCycleVal.substring(oldCycleVal.indexOf('R') + 1);
		String oldJDate = oldContentSbfFilenameParts[4];

		if (newCycleVal == null || newJDate == null || !newCycleVal.equalsIgnoreCase(oldCycleVal)
				|| !newJDate.equalsIgnoreCase(oldJDate)) {

			throw new Exception("New Audit file cycle and jdate not matching with old file");
		}
	}

	private void addHeaderForMasterFile(List<String> reports, String masterFileName) {

		String fileNameCyclePart = masterFileName.substring(masterFileName.indexOf(".") + 1);
		fileNameCyclePart = fileNameCyclePart.substring(fileNameCyclePart.indexOf(".") + 1);
		fileNameCyclePart = fileNameCyclePart.substring(0, fileNameCyclePart.indexOf("."));

		StringBuilder headerStr = new StringBuilder();
		headerStr.append("01");
		headerStr.append(" ");
		headerStr.append(fileNameCyclePart);
		reports.add(0, headerStr.toString());
	}

	private String prepareMasterFileName(Map<String, String> fields, String afpFileName) {

		String SBFName = "SB950.BBPR<PRGN>.R<CYCLE>.AFPRECON.<JDATE>";

		String prgnVal = getPRGNVALFromAFPFileName(afpFileName);
		String cycleVal = getCYCLEVALFromAFPFileName(afpFileName);
		String jdate = fields.get("CycleDate");

		SBFName = SBFName.replace("<PRGN>", prgnVal);
		SBFName = SBFName.replace("<CYCLE>", cycleVal);
		SBFName = SBFName.replace("<JDATE>", jdate);

		return SBFName;
	}

	private void createMasterFile(List<String> reports, String inputFolderPath, String masterFileName) {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			StringBuilder outputFilePath = new StringBuilder();
			outputFilePath.append(inputFolderPath);
			outputFilePath.append("\\");
			outputFilePath.append(masterFileName);
			outputFilePath.append(".txt");

			File file = new File(outputFilePath.toString());

			File parentFile = file.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}

			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for (String content : reports) {
				bw.write(content);
				bw.newLine();
				bw.flush();
			}

		} catch (Exception e) {
			System.out.println("Error while writing content to audit file is:" + e.getMessage());
		} finally {
			closeStream(bw);
			closeStream(fw);
		}
	}

	private void writeAuditStringtoFile(String auditFileName, String content, String inputFolderPath) {

		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			StringBuilder outputFilePath = new StringBuilder();
			outputFilePath.append(inputFolderPath);
			outputFilePath.append("\\");
			outputFilePath.append("auditfiles");
			outputFilePath.append("\\");
			outputFilePath.append(auditFileName);
			outputFilePath.append(".txt");

			System.out.println(outputFilePath.toString());

			File file = new File(outputFilePath.toString());
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			bw.write(content);
		} catch (Exception e) {
			System.out.println("Error while writing content to audit file is:" + e.getMessage());
		} finally {
			closeStream(bw);
			closeStream(fw);
		}
	}

	private String getAuditContent(Map<String, String> fields, String afpFileName, List<String> reports) {

		StringBuilder auditStr = new StringBuilder();
		String SBFName = "SB950.BBPR<PRGN>.R<FVAL><CYCLE>.AFPP.<JDATE>";

		auditStr.append("01");
		auditStr.append(" ");

		String stmtCountVal = fields.get("Statements");
		if (stmtCountVal != null) {
			stmtCountVal = stmtCountVal.replace(",", "");
			if (stmtCountVal.length() < 10) {
				stmtCountVal = String.format("%010d", Integer.parseInt(stmtCountVal));
			}
			auditStr.append(stmtCountVal);
			auditStr.append(" ");
		}

		String prgnVal = getPRGNVALFromAFPFileName(afpFileName);
		String cycleVal = getCYCLEVALFromAFPFileName(afpFileName);

		String fileSeq = fields.get("FileSequence");
		if (fileSeq != null) {
			fileSeq = fileSeq.replace(",", "");
		}
		int FVAL = Integer.parseInt(fileSeq) - 1;

		String jdate = fields.get("CycleDate");
		if (jdate != null) {
			jdate = jdate.replace(",", "");
		}

		SBFName = SBFName.replace("<PRGN>", prgnVal);
		SBFName = SBFName.replace("<FVAL>", String.format("%02d", FVAL));
		SBFName = SBFName.replace("<CYCLE>", cycleVal);
		SBFName = SBFName.replace("<JDATE>", jdate);

		auditStr.append(SBFName);
		auditStr.append(" ");

		String envCountVal = fields.get("Envelopes");
		if (envCountVal != null) {
			envCountVal = envCountVal.replace(",", "");
			if (envCountVal.length() < 10) {
				envCountVal = String.format("%010d", Integer.parseInt(envCountVal));
			}
			auditStr.append(envCountVal);
			auditStr.append(" ");
		}

		reports.add(auditStr.toString());
		String auditFileName = SBFName.replace("AFPP", "AFPRECON");

		return auditFileName;
	}

	private String getPRGNVALFromAFPFileName(String afpFileName) {

		int startIndex = -1;
		for (int i = 0; i < 4; i++) {
			startIndex = afpFileName.indexOf(".", startIndex + 1);
		}
		int endIndex = afpFileName.indexOf(".", startIndex + 1);

		String result = afpFileName.substring(startIndex + 1, endIndex);
		return result;
	}

	private String getCYCLEVALFromAFPFileName(String afpFileName) {

		int startIndex = -1;
		for (int i = 0; i < 5; i++) {
			startIndex = afpFileName.indexOf(".", startIndex + 1);
		}
		int endIndex = afpFileName.indexOf(".", startIndex + 1);

		String result = afpFileName.substring(startIndex + 1, endIndex);
		return result;
	}

	private Map<String, String> readParamFile(File paramFile) {
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		Map<String, String> fields = new HashMap<String, String>();
		try {
			fileReader = new FileReader(paramFile);
			bufferedReader = new BufferedReader(fileReader);
			String line;

			while ((line = bufferedReader.readLine()) != null) {

				line = line.trim();
				int equalSymbolIndex = line.indexOf("=");

				if (equalSymbolIndex > -1) {
					String key = line.substring(0, equalSymbolIndex);
					String value = line.substring(equalSymbolIndex + 1);

					if (key != null && value != null) {
						fields.put(key.trim(), value.trim());
					}
				}
			}
			System.out.println(fields);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeStream(bufferedReader);
			closeStream(fileReader);
		}
		return fields;
	}

	private void closeStream(Closeable stream) {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (Exception e) {
			System.out.println("Exception while closing a stream is:" + e.getMessage());
		}
	}

}
