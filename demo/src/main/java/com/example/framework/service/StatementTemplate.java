package main.java.com.example.framework.service;


import freemarker.template.Template;

/**
 * StatementTemplate 存储动态SQL的对象
 * Created by leon_zy on 2018/11/16
 */
public class StatementTemplate {
    public enum TYPE {
        /** hql 查询 */
        HQL,
        /** sql 查询 */
        SQL
    }

    public StatementTemplate() {

    }

    public StatementTemplate(TYPE type, Template template) {

        this.type = type;

        this.template = template;

    }

    private TYPE type;

    private Template template;

    public TYPE getType() {

        return type;

    }

    public void setType(TYPE type) {

        this.type = type;

    }

    public Template getTemplate() {

        return template;

    }

    public void setTemplate(Template template) {

        this.template = template;

    }
}