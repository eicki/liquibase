package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;
import liquibase.util.StringUtils;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;

public class SelectFromDatabaseChangeLogGenerator implements SqlGenerator<SelectFromDatabaseChangeLogStatement> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(SelectFromDatabaseChangeLogStatement statement, Database database) {
        return true;
    }

    public ValidationErrors validate(SelectFromDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("columnToSelect", statement.getColumnsToSelect());

        return errors;
    }

    public Sql[] generateSql(SelectFromDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql = "SELECT " + StringUtils.join(statement.getColumnsToSelect(), ",") + " FROM " +
                database.escapeTableName(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());

        SelectFromDatabaseChangeLogStatement.WhereClause whereClause = statement.getWhereClause();
        if (whereClause != null) {
            if (whereClause instanceof SelectFromDatabaseChangeLogStatement.ByTag) {
                sql += " WHERE tag='" + ((SelectFromDatabaseChangeLogStatement.ByTag) whereClause).getTagName() + "'";
            } else {
                throw new UnexpectedLiquibaseException("Unknown where clause type: " + whereClause.getClass().getName());
            }
        }

        if (statement.getOrderByColumns().length > 0) {
            sql += " ORDER BY "+StringUtils.join(statement.getOrderByColumns(), ", ");
        }

        return new Sql[]{
                new UnparsedSql(sql)
        };
    }
}