package exception;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.Part;

import org.json.simple.JSONArray;

import restws.Student;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class Validator {
	public static final int GENERAL = 0;
	public static final int TASK_ID = 1;
	public static final int TEMPLATE_ID = 2;
	public static final int USER_SUPERVISOR = 3;
	
	public static final int APPKEY = 1;
	public static final int TOKEN = 2;
	public static final int USERNAME = 3;
	public static final int PASSWORD = 4;
	public static final int NAME_PERSON = 5;
	public static final int NIM = 6;
	public static final int NPP = 7;
	public static final int ADDRESS = 8;
	public static final int PHONE_NUMBER = 9;
	public static final int EMAIL = 10;
	public static final int THESIS_TOPIC = 11;
	public static final int THESIS_TITLE = 12;
	public static final int THESIS_DESCRIPTION = 13;
	public static final int TASK_TEMPLATE_ID = 14;
	public static final int TASK_TEMPLATE_NAME = 15;
	public static final int TASK_TEMPLATE_DESCRIPTION = 16;
	public static final int TASK_STATUS = 17;
	public static final int TASK_DURATION = 18;
	public static final int RESPONSE = 19;
	public static final int FIELD_NAME = 20;
	public static final int FIELD_DESCRIPTION = 21;
	public static final int COMMENT_TYPE = 22;
	public static final int COMMENT_TEXT = 23;
	public static final int FILE_EXTENSION = 24;
	
	public static final int STUDENT_STATUS_IDLE = -1;
	public static final int STUDENT_STATUS_PROPOSE = 0;
	public static final int STUDENT_STATUS_ASSIGNED = 1;
	public static final int STUDENT_STATUS_ACTIVE = 2;
	public static final int STUDENT_STATUS_CLAIM = 3;
	public static final int STUDENT_STATUS_GRADUATE = 4;
	
	public static void isExist(Object validation, int validator) throws ExceptionValidation {
		if (validator == GENERAL) {
			if (validation == null ||
						(validation instanceof DBCursor && !((DBCursor) validation).hasNext()))
					throw new ExceptionValidation(ExceptionValidation.NOT_EXIST);
		}
		else if (validator == TASK_ID) {
			if (validation == null)
				throw new ExceptionValidation(ExceptionValidation.TASK_NOT_EXIST);
		}
		else if (validator == TEMPLATE_ID) {
			if (validation == null)
				throw new ExceptionValidation(ExceptionValidation.TEMPLATE_NOT_EXIST);
		}
		else if (validator == USER_SUPERVISOR) {
			if (validation == null)
				throw new ExceptionValidation(ExceptionValidation.SUPERVISOR_NOT_EXIST);
		}
	}
	
	public static void isExist(Object validation0, Object validation1) throws ExceptionValidation {
		if (validation0 == null && validation1 == null)
			throw new ExceptionValidation(ExceptionValidation.USER_NOT_EXIST);
	}
	
	public static void isNotExist(Object validation0, Object validation1) throws ExceptionValidation {
		if (validation0 != null || validation1 != null)
			throw new ExceptionValidation(ExceptionValidation.USERNAME_EXIST);
	}
	
	public static void isParameterEmpty(Object validation) throws ExceptionValidation {
		if (validation == null ||
				(validation instanceof String && ((String) validation).isEmpty()) ||
				(validation instanceof JSONArray && ((JSONArray) validation).isEmpty()) ||
				(validation instanceof Part && ((Part) validation) == null) ||
				(validation instanceof File && !((File) validation).exists()))
			throw new ExceptionValidation(ExceptionValidation.EMPTY_PARAMETER);
	}
	
	public static void isParameterEmpty(Object validation0, Object validation1) throws ExceptionValidation {
		if (((String) validation0).isEmpty() && ((Part) validation1) == null)
			throw new ExceptionValidation(ExceptionValidation.EMPTY_PARAMETER);
	}
	
	public static void isParameterWrong(Object validation, int validator) throws ExceptionValidation {
		if (validator == APPKEY) {
			if (!Pattern.matches("^[a-zA-Z\\d]{20}$", (CharSequence) validation))
				throw new ExceptionValidation(ExceptionValidation.WRONG_APPKEY);
		}
		else if (validator == TOKEN) {
			if (!Pattern.matches("^[a-zA-Z\\d]{20}$", (CharSequence) validation))
				throw new ExceptionValidation(ExceptionValidation.WRONG_TOKEN);
		}
		else if (validator == USERNAME) {
			if (!Pattern.matches("^[\\w\\-\\.]{4,15}$", (CharSequence) validation))
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == PASSWORD) {
			if (!Pattern.matches("^\\w{8,16}$", (CharSequence) validation))
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == NIM) {
			if (!Pattern.matches("^[A-E]\\d{2}\\.\\d{4}\\.\\d{5}$", (CharSequence) validation))
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == NPP) {
			if (!Pattern.matches("^\\d{4}\\.\\d{2}\\.\\d{4}\\.\\d{3}$", (CharSequence) validation))
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == NAME_PERSON) {
			if (((String) validation).length() > 30)
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == ADDRESS) {
			if (((String) validation).length() > 50)
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == PHONE_NUMBER) {
			if (!Pattern.matches("^\\d{0,20}$", (CharSequence) validation))
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == EMAIL) {
			if (!Pattern.matches("^[^@\\s]+@[^\\s\\.]+\\.[^\\s\\.]+$", (CharSequence) validation) &&
					!((String) validation).isEmpty())
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == THESIS_TOPIC) {
			if (((String) validation).length() > 150)
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == THESIS_TITLE) {
			if (((String) validation).length() > 100)
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == THESIS_DESCRIPTION) {
			List<String> arrayList = new ArrayList<String>();
			for (String string : ((String) validation).split("\\s")) {
				arrayList.add(string);
			}
			if (arrayList.size() > 250)
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == TASK_TEMPLATE_ID) {
			if (!Pattern.matches("^[a-zA-Z\\d]{5}$", (CharSequence) validation))
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == TASK_TEMPLATE_NAME) {
			if (((String) validation).length() > 20)
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == TASK_TEMPLATE_DESCRIPTION) {
			if (((String) validation).length() > 100)
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == TASK_STATUS) {
			if ((int) validation == 1)
				throw new ExceptionValidation(ExceptionValidation.WRONG_TASK_STATUS);
		}
		else if (validator == TASK_DURATION) {
			if (!((String) validation).isEmpty())
				if (((String) validation).length() > 3 ||
						Integer.parseInt((String) validation) <= 0)
					throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == RESPONSE) {
			if ((int) validation != 0 ||
					(int) validation != 1)
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == FIELD_NAME) {
			if (((String) validation).length() > 25)
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == FIELD_DESCRIPTION) {
			if (((String) validation).length() > 100)
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == COMMENT_TYPE) {
			if ((int) validation != 11 ||
					(int) validation != 12 ||
					(int) validation != 13 ||
					(int) validation != 21 ||
					(int) validation != 22)
				throw new ExceptionValidation(ExceptionValidation.WRONG_COMMENT_TYPE);
		}
		else if (validator == COMMENT_TEXT) {
			if (((String) validation).length() > 200)
				throw new ExceptionValidation(ExceptionValidation.WRONG_PARAMETER);
		}
		else if (validator == FILE_EXTENSION) {
			String filename = ((Part) validation).getSubmittedFileName();
			String extension = filename.substring(filename.lastIndexOf('.')+1).toLowerCase();
			if (!extension.equals("pdf"))
				throw new ExceptionValidation(ExceptionValidation.WRONG_FILE_EXTENSION);
		}
	}
	
	public static void isParameterWrong(DBCollection collField, JSONArray field) throws ExceptionValidation {
		for (int i = 0; i < field.size(); i++) {
			DBObject queryObject = new BasicDBObject("_id", field.get(i).toString());
			DBObject objectField = collField.findOne(queryObject);
			if (objectField == null)
				throw new ExceptionValidation(ExceptionValidation.FIELD_NOT_EXIST);
		}
	}
	
	public static void isStudentStatus(int status, int validator) throws ExceptionValidation {
		if (validator == STUDENT_STATUS_IDLE) {
			if (status != Student.STATUS_IDLE)
				throw new ExceptionValidation(ExceptionValidation.WRONG_STUDENT_STATUS);
		}
		else if (validator == STUDENT_STATUS_PROPOSE) {
			if (status != Student.STATUS_PROPOSE)
				throw new ExceptionValidation(ExceptionValidation.WRONG_STUDENT_STATUS);
		}
		else if (validator == STUDENT_STATUS_ASSIGNED) {
			if (status != Student.STATUS_ASSIGN)
				throw new ExceptionValidation(ExceptionValidation.WRONG_STUDENT_STATUS);
		}
		else if (validator == STUDENT_STATUS_ACTIVE) {
			if (status != Student.STATUS_ACTIVE)
				throw new ExceptionValidation(ExceptionValidation.WRONG_STUDENT_STATUS);
		}
	}
}
