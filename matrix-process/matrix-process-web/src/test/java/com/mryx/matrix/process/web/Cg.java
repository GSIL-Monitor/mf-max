package com.mryx.matrix.process.web;

import org.springframework.util.CollectionUtils;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 *自定义生成 *.java、*.xml、*Mapper.java *Service.java I*Service
 *
 *@author hailin.su
 *@version 2.0.0
 */
public class Cg {

    /**
     **********************************使用前必读*******************
     **
     ** 使用需要修改的值：{
     *										1-dbName：数据库名；
     *										2-user：数据库名；
     *										3-password：数据库密码；
     *										4-url:数据库连接；
     *	 }
     **
     ***********************************************************
     */

    private final String type_char = "char";

    private final String type_varchar = "varchar";

    private final String type_date = "date";

    private final String type_datetime = "datetime";

    private final String type_timestamp = "timestamp";

    private final String type_int = "int";

    private final String type_bigint = "bigint";

    private final String type_text = "text";

    private final String type_bit = "bit";

    private final String type_decimal = "decimal";

    private final String type_blob = "blob";

    private final String type_float = "float";

    private final String type_double = "double";

    private final String type_enum = "enum";

    /**
     * 修改的地方
     */

    private final String moduleName = ""; // 模块名

    private final String bean_package = "com.mryx.matrix.process.domain";

    private final String mapper_package = "com.mryx.matrix.process.core.dao";

    private final String service_package = "com.mryx.matrix.process.core.service.impl";

    private final String i_service_package = "com.mryx.matrix.process.core.service";

    private final String driverName = "com.mysql.jdbc.Driver";

    private final String user = "root";

    private final String password = "root";

//    private String url = "jdbc:mysql://10.7.2.16:3306/DB_NAME?useSSL=false";
    private String url = "jdbc:mysql://localhost:3306/test?useSSL=false&useUnicode=true&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true";

    private String tableName = null;

    private String beanName = null;

    private String mapperName = null;

    private Connection conn = null;


    /**
     * 加载驱动
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private void init(String dbName) throws ClassNotFoundException, SQLException {
        Class.forName(driverName);//"com.mysql.jdbc.Driver"
        url = url.replace("DB_NAME", dbName);
        conn = DriverManager.getConnection(url, user, password);
    }

    /**
     * 将表名转换为bean名称
     * @param table
     */
    private void processTable(String table) {
        StringBuffer sb = new StringBuffer(table.length());
//        String tableNew = table.toLowerCase();
        String[] tables = table.split("_");
        String temp = null;
        for ( int i = 0 ; i < tables.length ; i++ ) {
            temp = tables[i].trim();
            sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
        }
//        if(tables.length==1){
//            temp = table.trim();
//            sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
//        }
//////        for ( int i = 1 ; i < tables.length ; i++ ) {
//            temp = table.trim();
//            sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
//////        }
        beanName = sb.toString();
        mapperName = mapper_package+"."+beanName + "Dao";
    }

    /**
     * 将数据库字段类型转换为xml中的jdbc类型（有引号，在resultMap中使用）
     * @param type
     * @return
     */
    private String processXmlType(String type) {
        if ( type.indexOf(type_varchar) > -1 ) {
            return "jdbcType=\"VARCHAR\"";
        } if ( type.indexOf(type_char) > -1 ) {
            return "jdbcType=\"CHAR\"";
        } else if ( type.indexOf(type_bigint) > -1 ) {
            return "jdbcType=\"BIGINT\"";
        } else if ( type.indexOf(type_int) > -1 ) {
            return "jdbcType=\"INTEGER\"";
        }else if ( type.indexOf(type_datetime) > -1 ) {
            return "jdbcType=\"TIMESTAMP\"";
        }  else if ( type.indexOf(type_date) > -1 ) {
            return "jdbcType=\"DATE\"";
        }else if ( type.indexOf(type_text) > -1 ) {
            return "jdbcType=\"LONGVARCHAR\"";
        } else if ( type.indexOf(type_timestamp) > -1 ) {
            return "jdbcType=\"TIMESTAMP\"";
        } else if ( type.indexOf(type_bit) > -1 ) {
            return "jdbcType=\"BIT\"";
        } else if ( type.indexOf(type_decimal) > -1 ) {
            return "jdbcType=\"DECIMAL\"";
        } else if ( type.indexOf(type_blob) > -1 ) {
            return "jdbcType=\"BLOB\"";
        }else if ( type.indexOf(type_float) > -1 ) {
            return "jdbcType=\"FLOAT\"";
        }else if ( type.indexOf(type_double) > -1 ) {
            return "jdbcType=\"DOUBLE\"";
        }else if ( type.indexOf(type_enum) > -1 ) {
            return "jdbcType=\"VARCHAR\"";
        }
        return null;
    }

    /**
     * 将数据库字段类型转换为xml中的jdbc类型(去掉引号，在sql语句中使用)
     * @param type
     * @return
     */
    private String processXmlType2( String type ) {
        if ( type.indexOf(type_varchar) > -1 ) {
            return "jdbcType=VARCHAR";
        } if ( type.indexOf(type_char) > -1 ) {
            return "jdbcType=CHAR";
        } else if ( type.indexOf(type_bigint) > -1 ) {
            return "jdbcType=BIGINT";
        } else if ( type.indexOf(type_int) > -1 ) {
            return "jdbcType=INTEGER";
        } else if ( type.indexOf(type_datetime) > -1 ) {
            return "jdbcType=TIMESTAMP";
        } else if ( type.indexOf(type_date) > -1 ) {
            return "jdbcType=DATE";
        }else if ( type.indexOf(type_text) > -1 ) {
            return "jdbcType=LONGVARCHAR";
        } else if ( type.indexOf(type_timestamp) > -1 ) {
            return "jdbcType=TIMESTAMP";
        } else if ( type.indexOf(type_bit) > -1 ) {
            return "jdbcType=BIT";
        } else if ( type.indexOf(type_decimal) > -1 ) {
            return "jdbcType=DECIMAL";
        } else if ( type.indexOf(type_blob) > -1 ) {
            return "jdbcType=BLOB";
        }else if ( type.indexOf(type_float) > -1 ) {
            return "jdbcType=FLOAT";
        }else if ( type.indexOf(type_double) > -1 ) {
            return "jdbcType=DOUBLE";
        }else if ( type.indexOf(type_enum) > -1 ) {
            return "jdbcType=VARCHAR";
        }
        return null;
    }

    /**
     * 将数据库字段类型转换为java类型
     * @param type
     * @return
     */
    private String processType(String type) {
        if ( type.indexOf(type_char) > -1 ) {
            return "String";
        } else if ( type.indexOf(type_bigint) > -1 ) {
            return "Long";
        } else if ( type.indexOf(type_int) > -1 ) {
            return "Integer";
        } else if ( type.indexOf(type_date) > -1 ) {
            return "Date";
        } else if ( type.indexOf(type_text) > -1 ) {
            return "String";
        } else if ( type.indexOf(type_timestamp) > -1 ) {
            return "Date";
        } else if ( type.indexOf(type_bit) > -1 ) {
            return "Boolean";
        } else if ( type.indexOf(type_decimal) > -1 ) {
            return "java.math.BigDecimal";
        } else if ( type.indexOf(type_blob) > -1 ) {
            return "byte[]";
        }else if ( type.indexOf(type_float) > -1 ) {
            return "Float";
        }else if ( type.indexOf(type_double) > -1 ) {
            return "Double";
        }else if ( type.indexOf(type_enum) > -1 ) {
            return "String";
        }
        return null;
    }

    /**
     * 将数据库字段名转化为bean字段名
     * @param field
     * @return
     */
    private String processField( String field ) {
        StringBuffer sb = new StringBuffer(field.length());
        //field = field.toLowerCase();
        String[] fields = field.split("_");
        String temp = null;
        sb.append(fields[0]);
        for ( int i = 1 ; i < fields.length ; i++ ) {
            temp = fields[i].trim();
            sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
        }
        return sb.toString();
    }


    /**
     * 实体用全名
     *
     * @param beanName
     * @return
     */
    private String processResultMapId(String beanName) {
//        return beanName.substring(0, 1).toLowerCase() + beanName.substring(1);//用别名
        return bean_package+"."+beanName;//用全名
    }

    /**
     *  将实体类名首字母改为小写
     * @param beanName
     * @return
     */
    private String processResultMapId2( String beanName ) {
        return beanName.substring(0, 1).toLowerCase() + beanName.substring(1);//用别名
    }

    /**
     *  构建类上面的注释
     *
     * @param bw
     * @param text
     * @return
     * @throws IOException
     */
    private BufferedWriter buildClassComment(BufferedWriter bw, String text) throws IOException {
        bw.newLine();
        bw.newLine();
        bw.write("/**");
        bw.newLine();
        bw.write(" * " + text);
        bw.newLine();
        bw.write(" * @author pengcheng");
        bw.newLine();
        bw.write(" * @email pengcheng@missfresh.cn");
        bw.newLine();
        bw.write(" * @date " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        bw.newLine();
        bw.write(" **/");
        return bw;
    }

    /**
     *  生成实体对象
     * @param columns
     * @param types
     * @param comments
     * @throws IOException
     */
    private void buildEntityBean(List<String> columns, List<String> types, List<String> comments, String tableComment, String beanPath) throws IOException {
        File folder = new File(beanPath);
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        File beanFile = new File(beanPath, beanName + ".java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
        bw.write("package " + bean_package + ";");
        bw.newLine();
        bw.write("import java.io.Serializable;");
        bw.newLine();
        bw.write("import java.util.Date;");
        bw.newLine();
        bw.write("import java.util.List;");
        bw.newLine();
        bw.write("import java.util.Map;");
        bw.newLine();

        bw = buildClassComment(bw, tableComment);
        bw.newLine();

        bw.write("public class " + beanName + " implements Serializable {");
        bw.newLine();
        bw.newLine();

        bw.write("\tprivate static final long serialVersionUID = "+getRandomNum(19)+"L;");
        bw.newLine();
        bw.newLine();

        int size = columns.size();
        for ( int i = 0 ; i < size ; i++ ) {
            bw.write("\t/**" + comments.get(i) + "**/");
            bw.newLine();
            bw.write("\tprivate " + processType(types.get(i)) + " " + processField(columns.get(i)) + ";");
            bw.newLine();
            bw.newLine();
        }
        bw.newLine();
        // 生成get 和 set方法
        String tempField = null;
        String _tempField = null;
        String tempType = null;
        for ( int i = 0 ; i < size ; i++ ) {
            tempType = processType(types.get(i));
            _tempField = processField(columns.get(i));
            tempField = _tempField.substring(0, 1).toUpperCase() + _tempField.substring(1);
            bw.newLine();
            //          bw.write("\tpublic void set" + tempField + "(" + tempType + " _" + _tempField + "){");
            bw.write("\tpublic void set" + tempField + "(" + tempType + " " + _tempField + "){");
            bw.newLine();
            //          bw.write("\t\tthis." + _tempField + "=_" + _tempField + ";");
            bw.write("\t\tthis." + _tempField + " = " + _tempField + ";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();
            bw.write("\tpublic " + tempType + " get" + tempField + "(){");
            bw.newLine();
            bw.write("\t\treturn this." + _tempField + ";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
        }
        bw.newLine();

        //重写toString
        bw.write("\t@Override");
        bw.newLine();
        bw.write("\tpublic String toString() {");
        bw.newLine();
        String _field = null;
        bw.write("\t\treturn \""+beanName+" [ ");
        for ( int i = 0 ; i < size ; i++ ) {
            _field = processField(columns.get(i));
            bw.write(_field+"= \"+"+_field);
            if((i+1)<size){
                bw.write("+");
                bw.newLine();
                bw.write("\t\t\t\",");
            }
        }
        bw.write("+\"]\";");
        bw.newLine();
        bw.write("\t}");
        bw.newLine();


        bw.write("}");
        bw.newLine();
        bw.flush();
        bw.close();
    }


    /**
     *  构建实体类映射XML文件
     *
     * @param columns
     * @param types
     * @param comments
     * @throws IOException
     */
    private void buildMapperXml(List<String> columns, List<String> types, List<String> comments ,String tableComment, String xmlPath) throws IOException {
        File folder = new File(xmlPath);
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        File mapperXmlFile = new File(xmlPath, beanName + "Mapper.xml");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperXmlFile)));
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        bw.newLine();
        bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" ");
        bw.write("    \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
        bw.newLine();
        bw.write("<mapper namespace=\"" + mapperName + "\">");
        bw.newLine();
        bw.newLine();

        //通用结果集BaseResultMap的生成
//        bw.write("\t<!--cg generate by hailin.su at "+new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())+"-->");
//        bw.newLine();
//        bw.newLine();

        bw.write("\t<!--通用"+tableComment+"表映射-->");
        bw.newLine();
        bw.write("\t<resultMap id=\"BaseResultMap\" type=\""+processResultMapId(beanName)+"\" >");
        bw.newLine();
/*        bw.write("\t\t<id property=\"" + this.processField(columns.get(0)) + "\" column=\"" + columns.get(0) +"\" "
        			+this.processXmlType(types.get(0))+ " />");
        bw.newLine();*/
        int size = columns.size();
        bw.write("\t\t<id property=\"" + this.processField(columns.get(0)) + "\" column=\"" + columns.get(0) +"\" "
                +this.processXmlType(types.get(0))+ " />");
        bw.newLine();
        for ( int i = 1 ; i < size ; i++ ) {
            bw.write("\t\t<result property=\"" + this.processField(columns.get(i)) + "\" column=\"" + columns.get(i) +"\" "
                    +this.processXmlType(types.get(i))+ " />");
            bw.newLine();
        }
        bw.write("\t</resultMap>");

        bw.newLine();
        bw.newLine();
        bw.newLine();


        // 下面开始写SqlMapper中的方法
        buildSQL(bw, columns, types);

        bw.write("</mapper>");
        bw.flush();
        bw.close();
    }


    /**
     *  生成mapper类
     *
     * @param types
     * @throws IOException
     */
    private void buildMapper(List<String> types,String tableComment, String mapperPath)throws IOException {
        File folder = new File(mapperPath);
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        File beanFile = new File(mapperPath, beanName + "Dao.java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
        bw.write("package " + mapper_package + ";");
        bw.newLine();
        bw.newLine();

        bw.write("import java.util.List;");
        bw.newLine();
        bw.write("import java.util.Date;");
        bw.newLine();
        bw.write("import java.util.Map;");
        bw.newLine();
        bw.newLine();

        bw.write("import "+bean_package+"."+beanName+";");
        bw.newLine();
        bw.newLine();

        bw = buildClassComment(bw, tableComment);
        bw.newLine();
        bw.write("public interface " + beanName + "Dao {");
        bw.newLine();
        bw.newLine();

        bw.write("\t" + beanName + " getById("+processType(types.get(0))+" id);");
        bw.newLine();
        bw.newLine();

//        bw.write("\tint deleteByPrimaryKey("+processType(types.get(0))+" id);");
//        bw.newLine();
//        bw.newLine();

//        bw.write("\tint deleteByPrimaryKey("+beanName+" "+processResultMapId2(beanName)+");");
//        bw.newLine();
//        bw.newLine();

        bw.write("\tint insert("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tint updateById("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

//        bw.write("\tint updateById("+beanName+" "+processResultMapId2(beanName)+");");
//        bw.newLine();
//        bw.newLine();

        bw.write("\tint pageTotal("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tList<" + beanName + "> listPage("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tList<" + beanName + "> listByCondition("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("}");
        bw.newLine();
        bw.flush();
        bw.close();
    }

    /**
     *  生成service接口
     *
     * @param types
     * @throws IOException
     */
    private void buildInterService(List<String> types,String tableComment, String iServicePath)throws IOException {
        File folder = new File(iServicePath);
        if ( !folder.exists() ) {
            folder.mkdirs();
        }
        String className = beanName + "Service";

        File beanFile = new File(iServicePath,className+".java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
        bw.write("package " + i_service_package + ";");
        bw.newLine();
        bw.newLine();

        bw.write("import java.util.List;");
        bw.newLine();
        bw.write("import java.util.Date;");
        bw.newLine();
        bw.write("import java.util.Map;");
        bw.newLine();
        bw.newLine();

        bw.write("import "+bean_package+"."+beanName+";");
        bw.newLine();
        bw.newLine();

        bw = buildClassComment(bw, tableComment);
        bw.newLine();
        bw.write("public interface " + className + " {");
        bw.newLine();
        bw.newLine();

        bw.write("\t" + beanName + " getById("+processType(types.get(0))+" id);");
        bw.newLine();
        bw.newLine();

//        bw.write("\tpublic int deleteByPrimaryKey("+processType(types.get(0))+" id);");
//        bw.newLine();
//        bw.newLine();

//        bw.write("\tpublic int deleteByPrimaryKey("+beanName+" "+processResultMapId2(beanName)+");");
//        bw.newLine();
//        bw.newLine();

        bw.write("\tint insert("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tint updateById("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

//        bw.write("\tint updateByPrimaryKey("+beanName+" "+processResultMapId2(beanName)+");");
//        bw.newLine();
//        bw.newLine();

        bw.write("\tint pageTotal("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tList<" + beanName + "> listPage("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("\tList<" + beanName + "> listByCondition("+beanName+" "+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.newLine();

        bw.write("}");
        bw.newLine();
        bw.flush();
        bw.close();
    }

    /**
     *  生成service 实现类
     *
     * @param types
     * @throws IOException
     */
    private void buildImplService(List<String> types,String tableComment, String servicePath)throws IOException {
        File folder = new File(servicePath);
        if ( !folder.exists() ) {
            folder.mkdirs();
        }
        String className = beanName + "ServiceImpl";

        String i_className = beanName + "Service";

        File beanFile = new File(servicePath,className+".java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
        bw.write("package " + service_package + ";");
        bw.newLine();
        bw.newLine();

        bw.write("import java.util.List;");
        bw.newLine();
        bw.write("import java.util.Date;");
        bw.newLine();
        bw.write("import java.util.Map;");
        bw.newLine();
        bw.write("import org.slf4j.Logger;");
        bw.newLine();
        bw.write("import org.slf4j.LoggerFactory;");
        bw.newLine();
        bw.write("import org.springframework.stereotype.Service;");
        bw.newLine();
        bw.write("import javax.annotation.Resource;");
        bw.newLine();
        bw.write("import " + mapper_package + "." + beanName + "Dao;");
        bw.newLine();
        bw.write("import " + i_service_package + "." + i_className+";");
        bw.newLine();
        bw.newLine();

        bw.write("import "+bean_package+"."+beanName+";");
        bw.newLine();

        bw = buildClassComment(bw, tableComment);
        bw.newLine();
        String resoureName = processResultMapId2(beanName)+"Service";
        bw.write("@Service(\"" + resoureName + "\")");
        bw.newLine();
        bw.write("public class " + className + " implements " + i_className + " {");
        bw.newLine();
        bw.newLine();
        bw.write("\tprivate static Logger logger = LoggerFactory.getLogger("+className+".class);");
        bw.newLine();
        bw.newLine();
        bw.write("\t@Resource");bw.newLine();
        String daoName = processResultMapId2(beanName)+"Dao";
        bw.write("\tprivate "+beanName+"Dao "+daoName+";");
        bw.newLine();
        bw.newLine();

        bw.write("\t@Override");
        bw.newLine();
        bw.write("\tpublic " + beanName + " getById("+processType(types.get(0))+" id) {");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".getById(id);");bw.newLine();
        bw.write("\t}");

//        bw.write("\t@Override");
//        bw.write("\tpublic int deleteByPrimaryKey(" + processType(types.get(0)) + " id) {");
//        bw.newLine();
//        bw.newLine();
//        bw.write("\t\treturn "+daoName+".deleteByPrimaryKey(id);");bw.newLine();
//        bw.write("\t}");

        bw.newLine();
        bw.newLine();

        bw.write("\t@Override");
        bw.newLine();
        bw.write("\tpublic int insert(" + beanName + " " + processResultMapId2(beanName) + ") {");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".insert("+processResultMapId2(beanName)+");");bw.newLine();
        bw.write("\t}");

        bw.newLine();
        bw.newLine();

        bw.write("\t@Override");
        bw.newLine();
        bw.write("\tpublic int updateById(" + beanName + " " + processResultMapId2(beanName) + ") {");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".updateById("+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.write("\t}");
//        bw.write("\t@Override");
//        bw.newLine();
//        bw.write("\tpublic int updateByPrimaryKey(" + beanName + " " + processResultMapId2(beanName) + ") {");
//        bw.newLine();
//        bw.write("\t\treturn "+daoName+".updateByPrimaryKey("+processResultMapId2(beanName)+");");bw.newLine();
//        bw.write("\t}");

        bw.newLine();
        bw.newLine();
        bw.write("\t@Override");bw.newLine();
        bw.write("\tpublic int pageTotal(" + beanName + " " + processResultMapId2(beanName) + ") {");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".pageTotal("+processResultMapId2(beanName)+");");bw.newLine();
        bw.write("\t}");

        bw.newLine();
        bw.newLine();

        bw.write("\t@Override");bw.newLine();
        bw.write("\tpublic List<" + beanName + "> listPage(" + beanName + " " + processResultMapId2(beanName) + ") {");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".listPage("+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.write("\t}");

        bw.newLine();
        bw.newLine();

        bw.write("\t@Override");bw.newLine();
        bw.write("\tpublic List<" + beanName + "> listByCondition("+beanName+" "+processResultMapId2(beanName)+"){");
        bw.newLine();
        bw.write("\t\treturn "+daoName+".listByCondition("+processResultMapId2(beanName)+");");
        bw.newLine();
        bw.write("\t}");

        bw.newLine();
        bw.newLine();

        bw.write("}");
        bw.newLine();
        bw.flush();
        bw.close();
    }



    private void buildSQL( BufferedWriter bw, List<String> columns, List<String> types ) throws IOException {
        String tempField = null;

        int size = columns.size();

        // 通用结果列
        bw.write("\t<!-- 通用查询结果集合-->");
        bw.newLine();
        bw.write("\t<sql id=\"Base_Column_List\">");
        bw.newLine();

        bw.write("\t"+columns.get(0)+",");
        for ( int i = 1 ; i < size ; i++ ) {
            bw.write("\t" + columns.get(i));
            if ( i != size - 1 ) {
                bw.write(",");
            }
        }

        bw.newLine();
        bw.write("\t</sql>");
        bw.newLine();
        bw.newLine();

        //通用查询条件拼接
        bw.write("\t<!-- 公共查询条件-->");
        bw.newLine();
        bw.write("\t<!-- collection foreach DATE_FORMAT(create_time,'%Y-%m-%d') like CONCAT('%',#{goodsNo,jdbcType=VARCHAR},'%') -->");
        bw.newLine();
        bw.write("\t<!-- <![CDATA[<=]]> date_format(FROM_UNIXTIME(expire_time),'%Y-%c-%d %h:%i:%s') as showExpireTime-->");
        bw.newLine();
        bw.newLine();
        bw.write("\t<sql id=\"conditions\">");
        bw.newLine();

        for (int i = 0 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            //todo 数字类型=0的要特殊处理 delFlag != '' 若是0   false
            String type = this.processXmlType2(types.get(i));
            if ("jdbcType=INTEGER".equals(type)){
                bw.write("\t\t<if test=\"" + tempField + " != null \"> and "+ columns.get(i) + " = #{" + tempField + ","+type+"} </if>");
            }else {
                bw.write("\t\t<if test=\"" + tempField + " != null and "+tempField+" != '' \"> and "+ columns.get(i) + " = #{" + tempField + ","+type+"} </if>");
            }
            bw.newLine();
        }

        bw.write("\t</sql>");
        bw.newLine();
        bw.newLine();

        //通用排序语句
       /* bw.write("\t<!-- 通用排序语句拼接-->");
        bw.newLine();
        bw.write("\t<sql id=\"orderBy\">");
        bw.newLine();
//        bw.write("\t\t<if test=\"sort != null\"> order by #{sort,jdbcType=VARCHAR}");
        bw.write("\t\t<if test=\"sort != null\"> order by ${sort}");
        bw.newLine();
        bw.write("\t\t\t<choose>");
        bw.newLine();
        bw.write("\t\t\t\t<when test=\"flag==0\"> DESC </when>");
        bw.newLine();
        bw.write("\t\t\t\t<otherwise> ASC </otherwise>");
        bw.newLine();
        bw.write("\t\t\t</choose>");
        bw.newLine();
        bw.write("\t\t</if>");
        bw.newLine();
        bw.write("\t</sql>");
        bw.newLine();
        bw.newLine();*/


        // 查询（根据主键ID查询）
        bw.write("\t<!-- 查询（根据主键ID查询） -->");
        bw.newLine();
        bw.write("\t<select id=\"getById\" resultMap=\"BaseResultMap"
                + "\" parameterType=\"java.lang." + processType(types.get(0)) + "\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"Base_Column_List\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + ","+this.processXmlType2(types.get(0))+"}");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();
        // 查询完


        // 删除（根据主键ID删除）
//        bw.write("\t<!--删除：根据主键ID删除-->");
//        bw.newLine();
//        bw.write("\t<delete id=\"deleteByPrimaryKey\" parameterType=\"java.lang." + processType(types.get(0)) + "\">");
//        bw.newLine();
//        bw.write("\t\t DELETE FROM " + tableName);
//        bw.newLine();
//        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + ","+this.processXmlType2(types.get(0))+"}");
//        bw.newLine();
//        bw.write("\t</delete>");
//        bw.newLine();
//        bw.newLine();
        // 删除完


        //按条件删除
//        bw.write("\t<!-- 删除：根据输入条件删除 -->");
//        bw.newLine();
//        bw.write("\t<delete id=\"deleteByObject\" parameterType=\"" + processResultMapId(beanName) + "\">");
//        bw.newLine();
//        bw.write("\t\t DELETE FROM " + tableName);
//        bw.newLine();
//        bw.write("\t\t WHERE 1=1");
//        bw.newLine();
//        bw.write("\t\t <include refid=\"conditions\" />");
//        bw.newLine();
//        bw.write("\t</delete>");
//        bw.newLine();
//        bw.newLine();


        // 添加insert方法
        /*bw.write("\t<!-- 添加 -->");
        bw.newLine();
        bw.write("\t<insert id=\"insert\" parameterType=\"" + processResultMapId(beanName) + "\">");
        bw.newLine();
        bw.write("\t\t INSERT INTO " + tableName);
        bw.newLine();
        bw.write(" \t\t(");
        for ( int i = 0 ; i < size ; i++ ) {
            bw.write(columns.get(i));
            if ( i != size - 1 ) {
                bw.write(",");
            }
        }
        bw.write(") ");
        bw.newLine();
        bw.write("\t\t VALUES ");
        bw.newLine();
        bw.write(" \t\t(");
        for ( int i = 0 ; i < size ; i++ ) {
            bw.write("#{" + processField(columns.get(i)) + "}");
            if ( i != size - 1 ) {
                bw.write(",");
            }
        }
        bw.write(") ");
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
        bw.newLine();*/
        // 添加insert完


        //---------------  insert方法（匹配有值的字段）
        bw.write("\t<!-- 添加 （匹配有值的字段,不建议使用）-->");
        bw.newLine();
        bw.write("\t<insert id=\"insert\" parameterType=\"" + processResultMapId(beanName) + "\">");
        bw.newLine();
        bw.write("\t\t INSERT INTO " + tableName);
        bw.newLine();
        bw.write("\t\t <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >");
        bw.newLine();

        tempField = null;
        for ( int i = 0 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + " != null\"> "+ columns.get(i) + ",</if>");
            bw.newLine();
        }

        bw.write("\t\t </trim>");
        bw.newLine();

        bw.write("\t\t <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\" >");
        bw.newLine();

        tempField = null;
        for ( int i = 0 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + "!=null\">#{"+ tempField + ","+this.processXmlType2(types.get(i))+"},</if>");
            bw.newLine();
        }

        bw.write("\t\t </trim>");
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
        bw.newLine();
        //---------------  完毕


        // 修改update方法（匹配有值的字段）
        bw.write("\t<!-- 根据主键修改输入的值-->");
        bw.newLine();
        bw.write("\t<update id=\"updateById\" parameterType=\"" +processResultMapId(beanName) + "\">");
        bw.newLine();
        bw.write("\t\t UPDATE " + tableName);
        bw.newLine();
        bw.write(" \t\t <set> ");
        bw.newLine();

        tempField = null;
        for ( int i = 1 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + " != null\">"+ columns.get(i) + " = #{" + tempField + ","+this.processXmlType2(types.get(i))+"},</if>");
            bw.newLine();
        }

        bw.newLine();
        bw.write(" \t\t </set>");
        bw.newLine();
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + ","+this.processXmlType2(types.get(0))+"}");
        bw.newLine();
        bw.write("\t</update>");
        bw.newLine();
        bw.newLine();
        // update方法完毕

        // ----- 修改（全量修改）
//        bw.write("\t<!-- 根据主键全量修改,不建议使用-->");
//        bw.newLine();
//        bw.write("\t<update id=\"updateByPrimaryKey\" parameterType=\"" + processResultMapId(beanName) + "\">");
//        bw.newLine();
//        bw.write("\t\t UPDATE " + tableName);
//        bw.newLine();
//        bw.write("\t\t SET ");
//
//        bw.newLine();
//        tempField = null;
//        for ( int i = 1 ; i < size ; i++ ) {
//            tempField = processField(columns.get(i));
//            bw.write("\t\t\t " + columns.get(i) + " = #{" + tempField + "}");
//            if ( i != size - 1 ) {
//                bw.write(",");
//            }
//            bw.newLine();
//        }
//
//        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + "}");
//        bw.newLine();
//        bw.write("\t</update>");
//        bw.newLine();
//        bw.newLine();

        //分页查询
        bw.write("\t<!-- 分页查询 -->");
        bw.newLine();
        bw.write("\t<select id=\"listPage\" resultMap=\"BaseResultMap"
                + "\" parameterType=\"java.util.HashMap\" useCache=\"false\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"Base_Column_List\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE 1=1");
        bw.newLine();
        bw.write("\t\t <include refid=\"conditions\" />");
        bw.newLine();
        bw.write("\t\t ORDER BY id DESC");
        bw.newLine();
        bw.write("\t\t limit #{startOfPage},#{pageSize}");
//        bw.write("\t\t <include refid=\"orderBy\" />");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();

        //分页查询总数
        bw.write("\t<!-- 分页查询总数 -->");
        bw.newLine();
        bw.write("\t<select id=\"pageTotal\" resultType=\"java.lang.Integer"
                + "\" parameterType=\"java.util.HashMap\" useCache=\"false\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t count(*) ");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE 1=1");
        bw.newLine();
        bw.write("\t\t <include refid=\"conditions\" />");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();

        //按条件查询列表
        bw.write("\t<!-- 按条件查询列表 -->");
        bw.newLine();
        bw.write("\t<select id=\"listByCondition\" resultMap=\"BaseResultMap"
                + "\" parameterType=\"java.util.HashMap\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"Base_Column_List\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE 1=1");
        bw.newLine();
        bw.write("\t\t <include refid=\"conditions\" />");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();

    }


    /**
     *  获取所有的数据库表注释
     *
     * @return
     * @throws SQLException
     */
    private Map<String, String> getTableComment() throws SQLException {
        Map<String, String> maps = new HashMap<String, String>();
        PreparedStatement pstate = conn.prepareStatement("show table status");
        ResultSet results = pstate.executeQuery();
        while ( results.next() ) {
            String tableName = results.getString("NAME");
            String comment = results.getString("COMMENT");
            maps.put(tableName, comment);
        }
        return maps;
    }

    private String getRandomNum(int length){
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        code.append(String.valueOf(random.nextInt(90)+1));
        for (int i = 2; i < length; i++) {
            code.append(String.valueOf(random.nextInt(10)));
        }
        return code.toString();
    }

    /**
     *
     * @param tables 待生成的表
     * @param beanPath doamin存放路径
     * @param mapperPath dao存放路径
     * @param xmlPath xml存放路径
     * @param iServicePath service接口存放路径
     * @param servicePath service实现类存放路径
     * @return void
     *
     **/
    public void generate(String dbName, List<String> tables, String beanPath, String mapperPath, String xmlPath, String iServicePath, String servicePath) throws ClassNotFoundException, SQLException, IOException {
        init(dbName);
        String prefix = "show full fields from ";
        List<String> columns = null;
        List<String> types = null;
        List<String> comments = null;
        PreparedStatement pstate = null;
        if (CollectionUtils.isEmpty(tables)) {
            return;
        }
        Map<String, String> tableComments = getTableComment();
        for (String table : tables ) {
            columns = new ArrayList<String>();
            types = new ArrayList<String>();
            comments = new ArrayList<String>();
            pstate = conn.prepareStatement(prefix + table);
            ResultSet results = pstate.executeQuery();
            while ( results.next() ) {
                columns.add(results.getString("FIELD"));
                types.add(results.getString("TYPE"));
                comments.add(results.getString("COMMENT"));
            }
            tableName = table;
            processTable(table);
            //          this.outputBaseBean();
            String tableComment = tableComments.get(tableName);//表名注释
            buildEntityBean(columns, types, comments, tableComment, beanPath);
            buildMapperXml(columns, types, comments,tableComment, xmlPath);
            buildMapper(types,tableComment, mapperPath);
            buildInterService(types, tableComment, iServicePath);
            buildImplService(types, tableComment, servicePath);
        }
        conn.close();
    }


    public static void main( String[] args ) {
        String baseDir = System.getProperty("user.dir");
        String dbName = "matrix"; // 数据库名
        // domain 存放地址
        String beanPath = baseDir + "/matrix-process-api/src/main/java/com/mryx/matrix/process/domain/";
        // dao 存放地址
        String mapperPath = baseDir + "/matrix-process-core/src/main/java/com/mryx/matrix/process/core/dao/";
        // serviceImpl 存放地址
        String servicePath = baseDir + "/matrix-process-core/src/main/java/com/mryx/matrix/process/core/service/impl/";
        // service 接口存放地址
        String iServicePath = baseDir + "/matrix-process-core/src/main/java/com/mryx/matrix/process/core/service/";
        // xml 存放地址
        String xmlPath = baseDir + "/matrix-process-core/src/main/resources/ObjectMapper/";

        String[] array = {"app","app_deploy_config","app_server"};
        List<String> tables = Arrays.asList(array);
        System.out.println("beanPath: " + beanPath);
        System.out.println("mapperPath: " + mapperPath);
        System.out.println("servicePath: " + servicePath);
        System.out.println("iServicePath: " + iServicePath);
        System.out.println("xmlPath: " + xmlPath);
        try {
            new Cg().generate(dbName, tables, beanPath, mapperPath, xmlPath, iServicePath, servicePath);
            System.out.println("===============equ cg success======================");
        } catch (ClassNotFoundException e ) {
            e.printStackTrace();
        } catch (SQLException e ) {
            e.printStackTrace();
        } catch (IOException e ) {
            e.printStackTrace();
        }
    }
}