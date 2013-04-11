package com.rx201.dx.translator;

import com.android.dx.rop.code.Rop;
import com.android.dx.rop.code.Rops;

public class Scratchpad {
	private static Rop[] opcodes = {
		Rops.NOP, Rops.MOVE_INT, Rops.MOVE_LONG, Rops.MOVE_FLOAT, Rops.MOVE_DOUBLE, 
		Rops.MOVE_OBJECT, Rops.MOVE_RETURN_ADDRESS, Rops.MOVE_PARAM_INT, Rops.MOVE_PARAM_LONG,
		Rops.MOVE_PARAM_FLOAT, Rops.MOVE_PARAM_DOUBLE, Rops.MOVE_PARAM_OBJECT, Rops.CONST_INT,
		Rops.CONST_LONG, Rops.CONST_FLOAT, Rops.CONST_DOUBLE, Rops.CONST_OBJECT,
		Rops.CONST_OBJECT_NOTHROW, Rops.GOTO, Rops.IF_EQZ_INT, Rops.IF_NEZ_INT, Rops.IF_LTZ_INT,
		Rops.IF_GEZ_INT, Rops.IF_LEZ_INT, Rops.IF_GTZ_INT, Rops.IF_EQZ_OBJECT, Rops.IF_NEZ_OBJECT, 
		Rops.IF_EQ_INT, Rops.IF_NE_INT, Rops.IF_LT_INT, Rops.IF_GE_INT, Rops.IF_LE_INT, 
		Rops.IF_GT_INT, Rops.IF_EQ_OBJECT, Rops.IF_NE_OBJECT, Rops.SWITCH, Rops.ADD_INT, 
		Rops.ADD_LONG, Rops.ADD_FLOAT, Rops.ADD_DOUBLE, Rops.SUB_INT, Rops.SUB_LONG, Rops.SUB_FLOAT,
		Rops.SUB_DOUBLE, Rops.MUL_INT, Rops.MUL_LONG, Rops.MUL_FLOAT, Rops.MUL_DOUBLE, Rops.DIV_INT,
		Rops.DIV_LONG, Rops.DIV_FLOAT, Rops.DIV_DOUBLE, Rops.REM_INT, Rops.REM_LONG, Rops.REM_FLOAT,
		Rops.REM_DOUBLE, Rops.NEG_INT, Rops.NEG_LONG, Rops.NEG_FLOAT, Rops.NEG_DOUBLE, Rops.AND_INT,
		Rops.AND_LONG, Rops.OR_INT, Rops.OR_LONG, Rops.XOR_INT, Rops.XOR_LONG, Rops.SHL_INT,
		Rops.SHL_LONG, Rops.SHR_INT, Rops.SHR_LONG, Rops.USHR_INT, Rops.USHR_LONG, Rops.NOT_INT, 
		Rops.NOT_LONG, Rops.ADD_CONST_INT, Rops.ADD_CONST_LONG, Rops.ADD_CONST_FLOAT, 
		Rops.ADD_CONST_DOUBLE, Rops.SUB_CONST_INT, Rops.SUB_CONST_LONG, Rops.SUB_CONST_FLOAT, 
		Rops.SUB_CONST_DOUBLE, Rops.MUL_CONST_INT, Rops.MUL_CONST_LONG, Rops.MUL_CONST_FLOAT,
		Rops.MUL_CONST_DOUBLE, Rops.DIV_CONST_INT, Rops.DIV_CONST_LONG, Rops.DIV_CONST_FLOAT,
		Rops.DIV_CONST_DOUBLE, Rops.REM_CONST_INT, Rops.REM_CONST_LONG, Rops.REM_CONST_FLOAT, 
		Rops.REM_CONST_DOUBLE, Rops.AND_CONST_INT, Rops.AND_CONST_LONG, Rops.OR_CONST_INT,
		Rops.OR_CONST_LONG, Rops.XOR_CONST_INT, Rops.XOR_CONST_LONG, Rops.SHL_CONST_INT,
		Rops.SHL_CONST_LONG, Rops.SHR_CONST_INT, Rops.SHR_CONST_LONG, Rops.USHR_CONST_INT,
		Rops.USHR_CONST_LONG, Rops.CMPL_LONG, Rops.CMPL_FLOAT, Rops.CMPL_DOUBLE, Rops.CMPG_FLOAT,
		Rops.CMPG_DOUBLE, Rops.CONV_L2I, Rops.CONV_F2I, Rops.CONV_D2I, Rops.CONV_I2L, Rops.CONV_F2L, 
		Rops.CONV_D2L, Rops.CONV_I2F, Rops.CONV_L2F, Rops.CONV_D2F, Rops.CONV_I2D, Rops.CONV_L2D, 
		Rops.CONV_F2D, Rops.TO_BYTE, Rops.TO_CHAR, Rops.TO_SHORT, Rops.RETURN_VOID, Rops.RETURN_INT,
		Rops.RETURN_LONG, Rops.RETURN_FLOAT, Rops.RETURN_DOUBLE, Rops.RETURN_OBJECT, Rops.ARRAY_LENGTH,
		Rops.THROW, Rops.MONITOR_ENTER, Rops.MONITOR_EXIT, Rops.AGET_INT, Rops.AGET_LONG, 
		Rops.AGET_FLOAT, Rops.AGET_DOUBLE, Rops.AGET_OBJECT, Rops.AGET_BOOLEAN, Rops.AGET_BYTE, 
		Rops.AGET_CHAR, Rops.AGET_SHORT, Rops.APUT_INT, Rops.APUT_LONG, Rops.APUT_FLOAT,
		Rops.APUT_DOUBLE, Rops.APUT_OBJECT, Rops.APUT_BOOLEAN, Rops.APUT_BYTE, Rops.APUT_CHAR, 
		Rops.APUT_SHORT, Rops.NEW_INSTANCE, Rops.NEW_ARRAY_INT, Rops.NEW_ARRAY_LONG, 
		Rops.NEW_ARRAY_FLOAT, Rops.NEW_ARRAY_DOUBLE, Rops.NEW_ARRAY_BOOLEAN, Rops.NEW_ARRAY_BYTE,
		Rops.NEW_ARRAY_CHAR, Rops.NEW_ARRAY_SHORT, Rops.CHECK_CAST, Rops.INSTANCE_OF, 
		Rops.GET_FIELD_INT, Rops.GET_FIELD_LONG, Rops.GET_FIELD_FLOAT, Rops.GET_FIELD_DOUBLE,
		Rops.GET_FIELD_OBJECT, Rops.GET_FIELD_BOOLEAN, Rops.GET_FIELD_BYTE, Rops.GET_FIELD_CHAR,
		Rops.GET_FIELD_SHORT, Rops.GET_STATIC_INT, Rops.GET_STATIC_LONG, Rops.GET_STATIC_FLOAT,
		Rops.GET_STATIC_DOUBLE, Rops.GET_STATIC_OBJECT, Rops.GET_STATIC_BOOLEAN, 
		Rops.GET_STATIC_BYTE, Rops.GET_STATIC_CHAR, Rops.GET_STATIC_SHORT, Rops.PUT_FIELD_INT,
		Rops.PUT_FIELD_LONG, Rops.PUT_FIELD_FLOAT, Rops.PUT_FIELD_DOUBLE, Rops.PUT_FIELD_OBJECT,
		Rops.PUT_FIELD_BOOLEAN, Rops.PUT_FIELD_BYTE, Rops.PUT_FIELD_CHAR, Rops.PUT_FIELD_SHORT,
		Rops.PUT_STATIC_INT, Rops.PUT_STATIC_LONG, Rops.PUT_STATIC_FLOAT, Rops.PUT_STATIC_DOUBLE, 
		Rops.PUT_STATIC_OBJECT, Rops.PUT_STATIC_BOOLEAN, Rops.PUT_STATIC_BYTE, Rops.PUT_STATIC_CHAR,
		Rops.PUT_STATIC_SHORT, Rops.MARK_LOCAL_INT, Rops.MARK_LOCAL_LONG, Rops.MARK_LOCAL_FLOAT, 
		Rops.MARK_LOCAL_DOUBLE, Rops.MARK_LOCAL_OBJECT, Rops.FILL_ARRAY_DATA};
	
	public static void test() {
		System.out.println("----BRANCH_THROW----");
		for(int i=0; i<opcodes.length; i++) {
			if (opcodes[i].getBranchingness() == Rop.BRANCH_THROW)
				System.out.println(opcodes[i].toString());
		}
	}
}
