package io.zrz.sqldom;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.zrz.hai.sql.model.ImmutableArrayExpr;
import io.zrz.hai.sql.model.ImmutableBinaryExpr;
import io.zrz.hai.sql.model.ImmutableBoolExpr;
import io.zrz.hai.sql.model.ImmutableCastExpr;
import io.zrz.hai.sql.model.ImmutableFunctionExpr;
import io.zrz.hai.sql.model.ImmutableFunctionSelectSource;
import io.zrz.hai.sql.model.ImmutableIdentExpr;
import io.zrz.hai.sql.model.ImmutableIntExpr;
import io.zrz.hai.sql.model.ImmutableLimitClause;
import io.zrz.hai.sql.model.ImmutableNamedTable;
import io.zrz.hai.sql.model.ImmutableParamExpr;
import io.zrz.hai.sql.model.ImmutableSelectExpr;
import io.zrz.hai.sql.model.ImmutableSelectField;
import io.zrz.hai.sql.model.ImmutableSelectStatement;
import io.zrz.hai.sql.model.ImmutableStarExpression;
import io.zrz.hai.sql.model.ImmutableStringExpr;
import io.zrz.hai.sql.model.ImmutableWindowDefinition;
import io.zrz.hai.sql.model.ImmutableWindowFuncExpr;
import io.zrz.hai.sql.model.ImmutableWithClause;
import io.zrz.hai.sql.model.ModifiableCombineClause;
import io.zrz.hai.sql.model.ModifiableSelectStatement;
import io.zrz.hai.sql.model.ModifiableStatementSource;
import io.zrz.sqldom.model.BinaryExpr;
import io.zrz.sqldom.model.CastExpr;
import io.zrz.sqldom.model.FunctionExpr;
import io.zrz.sqldom.model.FunctionSelectSource;
import io.zrz.sqldom.model.LimitClause;
import io.zrz.sqldom.model.NamedTable;
import io.zrz.sqldom.model.NullExpr;
import io.zrz.sqldom.model.OrderByClause;
import io.zrz.sqldom.model.ParamExpr;
import io.zrz.sqldom.model.SelectField;
import io.zrz.sqldom.model.SqlExpr;
import io.zrz.sqldom.model.SqlStatement;
import io.zrz.sqldom.model.WindowFuncExpr;
import io.zrz.sqldom.model.CombineClause.CombineMode;
import io.zrz.sqldom.model.CombineClause.CombineType;

public class SqlModel {

  public static SelectField field(SqlStatement statement, String alias) {
    return field(ImmutableSelectExpr.builder().statement(statement).build(), alias);
  }

  public static ImmutableSelectField field(SqlExpr expr, String alias) {
    return ImmutableSelectField.builder().expression(expr).alias(alias).build();
  }

  public static ImmutableSelectField field(SqlExpr expr) {
    return ImmutableSelectField.builder().expression(expr).build();
  }

  public static CastExpr cast(SqlExpr expr, String type) {
    return ImmutableCastExpr.builder().expression(expr).type(type).build();
  }

  public static ImmutableWithClause.Builder withClause() {
    return ImmutableWithClause.builder();
  }

  public static NamedTable namedTable(String tableName) {
    return ImmutableNamedTable.builder().tableName(tableName).build();
  }

  public static NamedTable namedTable(String tableName, String alias) {
    return ImmutableNamedTable.builder().tableName(tableName).alias(alias).build();
  }

  public static ImmutableStarExpression star() {
    return ImmutableStarExpression.of();
  }

  public static ImmutableSelectStatement.Builder selectStatement() {
    return ImmutableSelectStatement.builder();
  }

  public static ImmutableWithClause.Builder with() {
    return ImmutableWithClause.builder();
  }

  public static FunctionExpr function(String func, SqlExpr... args) {
    return ImmutableFunctionExpr.builder().function(func).addParameter(args).build();
  }

  public static FunctionExpr function(String func, Iterable<SqlExpr> args) {
    return ImmutableFunctionExpr.builder().function(func).parameters(args).build();
  }

  public static BinaryExpr binop(String op, SqlExpr left, SqlExpr right) {
    return ImmutableBinaryExpr.builder().left(left).right(right).operator(op).build();
  }

  public static WindowFuncExpr windowFunction(FunctionExpr expr, OrderByClause orderBy) {
    return ImmutableWindowFuncExpr.builder().function(expr).window(ImmutableWindowDefinition.builder().orderBy(orderBy).build()).build();
  }

  public static WindowFuncExpr windowFunction(FunctionExpr expr) {
    return ImmutableWindowFuncExpr.builder().function(expr).window(ImmutableWindowDefinition.builder().build()).build();
  }

  public static WindowFuncExpr windowFunction(FunctionExpr expr, SqlExpr partition) {
    return ImmutableWindowFuncExpr.builder().function(expr).window(ImmutableWindowDefinition.builder().partitionBy(partition).build()).build();
  }

  public static ImmutableStringExpr strval(String val) {
    return ImmutableStringExpr.builder().value(val).build();
  }

  public static ImmutableIdentExpr ident(String val) {
    return ImmutableIdentExpr.builder().value(val).build();
  }

  public static ImmutableIdentExpr raw(final String sql) {
    return ImmutableIdentExpr.builder().value(sql).build();
  }

  public static ImmutableIntExpr intval(int val) {
    return ImmutableIntExpr.builder().value(val).build();
  }

  public static ImmutableBoolExpr boolval(boolean val) {
    return ImmutableBoolExpr.builder().value(val).build();
  }

  public static NullExpr nullval() {
    return NullExpr.getInstance();
  }

  public static LimitClause limit(Integer count) {
    return ImmutableLimitClause.builder().count(count).build();
  }

  public static ParamExpr placeholder(String varname) {
    return ImmutableParamExpr.builder().name(varname).build();
  }

  public static ParamExpr placeholder() {
    return ImmutableParamExpr.builder().build();
  }

  public static ModifiableSelectStatement unionize(ModifiableSelectStatement stmt, ModifiableSelectStatement... append) {
    for (final ModifiableSelectStatement add : append) {
      stmt = addUnion(stmt, add);
    }
    return stmt;
  }

  public static ModifiableSelectStatement addUnion(ModifiableSelectStatement stmt, ModifiableSelectStatement append) {
    if (stmt.combine() != null) {
      // it's already combined, so uphaul.
      final ModifiableCombineClause s = ModifiableCombineClause.create();
      s.setSource(stmt.combine().source());
      s.setMode(stmt.combine().mode());
      s.setType(stmt.combine().type());
      append.setCombine(s);
    }
    final ModifiableCombineClause s = ModifiableCombineClause.create();
    s.setMode(CombineMode.All);
    s.setType(CombineType.Union);
    s.setSource(append);
    stmt.setCombine(s);
    return stmt;
  }

  public static SqlExpr subselect(SqlStatement stmt) {
    return ImmutableSelectExpr.builder()
        .statement(stmt)
        .build();
  }

  public static ModifiableStatementSource source(SqlStatement select) {
    return ModifiableStatementSource.create().setStatement(select);
  }

  public static SqlExpr intArray(int[] values) {
    return ImmutableArrayExpr.builder().addAllValues(IntStream.of(values).mapToObj(val -> (SqlExpr) intval(val)).collect(Collectors.toList())).build();
  }

  public static FunctionSelectSource source(FunctionExpr func, String alias) {
    return ImmutableFunctionSelectSource.builder()
        .functionName(func.function())
        .addAllFunctionParameters(func.parameters())
        .alias(alias)
        .build();
  }

  public static FunctionSelectSource source(FunctionExpr func) {
    return ImmutableFunctionSelectSource.builder()
        .functionName(func.function())
        .addAllFunctionParameters(func.parameters())
        .build();
  }

}
