package exception;

public class ExceptionValidation extends Exception {
	private static final long serialVersionUID = 1L;

	private String message = "";
	public static final int WRONG_APPKEY = 0;
	public static final int WRONG_TOKEN = 1;
	public static final int TOKEN_EXPIRED = 2;
	public static final int EMPTY_PARAMETER = 3;
	public static final int WRONG_COMMENT_TYPE = 4;
	public static final int WRONG_FILE_EXTENSION = 5;
	public static final int WRONG_PARAMETER = 6;
	public static final int WRONG_STUDENT_STATUS = 7;
	public static final int WRONG_TASK_STATUS = 8;
	public static final int EXIST = 9;
	public static final int USERNAME_EXIST = 10;
	public static final int NOT_EXIST = 11;
	public static final int FIELD_NOT_EXIST = 12;
	public static final int FILE_NOT_EXIST = 13;
	public static final int SUPERVISOR_NOT_EXIST = 14;
	public static final int TASK_NOT_EXIST = 15;
	public static final int TEMPLATE_NOT_EXIST = 16;
	public static final int USER_NOT_EXIST = 17;
	public static final int WRONG_YEAR = 18;
	
	public ExceptionValidation(int code) {
		super();
		if (code == EMPTY_PARAMETER)
			message = "Parameter is Empty!";
		
		else if (code == WRONG_PARAMETER)
			message = "Wrong Parameter!";
		
		else if (code == WRONG_APPKEY)
			message = "Appkey is Wrong!";

		else if (code == WRONG_COMMENT_TYPE)
			message = "Error Comment Type!";
		
		else if (code == WRONG_FILE_EXTENSION)
			message = "Wrong File Extension!";

		else if (code == WRONG_STUDENT_STATUS)
			message = "Wrong Student Status!";

		else if (code == WRONG_TASK_STATUS)
			message = "Task Already Done!";
		
		else if (code == WRONG_TOKEN)
			message = "Token is Wrong.";

		else if (code == TOKEN_EXPIRED)
			message = "Token was Expired. Please Login Again!";

		else if (code == EXIST)
			message = "Data Already Exist!";
		
		else if (code == USERNAME_EXIST)
			message = "Username Already Exist!";
		
		else if (code == NOT_EXIST)
			message = "Data Doesn't Exist!";
		
		else if (code == FIELD_NOT_EXIST)
			message = "Field Doesn't Exist!";
		
		else if (code == FILE_NOT_EXIST)
			message = "File Doesn't Exist!";

		else if (code == TASK_NOT_EXIST)
			message = "Task Doesn't Exist!";
		
		else if (code == TEMPLATE_NOT_EXIST)
			message = "Code Template Doesn't Exist!";
		
		else if (code == SUPERVISOR_NOT_EXIST)
			message = "You're not a Supervisor!";

		else if (code == USER_NOT_EXIST)
			message = "Cannot Find User!";
		else if (code == WRONG_YEAR)
			message = "Year must be 4 digits!";
		else
			message = "Code for validation is not valid!";
	}
	
	@Override
	public String toString() {
		return message;

	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
