package io.zrz.sqldom;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.postgresql.core.Utils;

import com.google.common.collect.Maps;

import io.zrz.sqldom.model.AggrFuncExpr;
import io.zrz.sqldom.model.ArrayExpr;
import io.zrz.sqldom.model.BinaryExpr;
import io.zrz.sqldom.model.BoolExpr;
import io.zrz.sqldom.model.CastExpr;
import io.zrz.sqldom.model.CombineClause;
import io.zrz.sqldom.model.ConflictAction;
import io.zrz.sqldom.model.ConflictTarget;
import io.zrz.sqldom.model.ConstraintConflictTarget;
import io.zrz.sqldom.model.ForLockClause;
import io.zrz.sqldom.model.FunctionExpr;
import io.zrz.sqldom.model.FunctionSelectSource;
import io.zrz.sqldom.model.GroupByClause;
import io.zrz.sqldom.model.HavingClause;
import io.zrz.sqldom.model.IdentExpr;
import io.zrz.sqldom.model.InsertSourceVisitor;
import io.zrz.sqldom.model.InsertStatement;
import io.zrz.sqldom.model.IntExpr;
import io.zrz.sqldom.model.JoinSource;
import io.zrz.sqldom.model.NamedTable;
import io.zrz.sqldom.model.NullExpr;
import io.zrz.sqldom.model.OrderByClause;
import io.zrz.sqldom.model.ParamExpr;
import io.zrz.sqldom.model.RawExpr;
import io.zrz.sqldom.model.SelectExpr;
import io.zrz.sqldom.model.SelectField;
import io.zrz.sqldom.model.SelectSource;
import io.zrz.sqldom.model.SelectStatement;
import io.zrz.sqldom.model.SourceRef;
import io.zrz.sqldom.model.SqlExpr;
import io.zrz.sqldom.model.SqlStatement;
import io.zrz.sqldom.model.StatementSource;
import io.zrz.sqldom.model.StringExpr;
import io.zrz.sqldom.model.TableRefExpr;
import io.zrz.sqldom.model.UpdateConflictAction;
import io.zrz.sqldom.model.ValueListSource;
import io.zrz.sqldom.model.WindowClause;
import io.zrz.sqldom.model.WindowDefinition;
import io.zrz.sqldom.model.WindowFuncExpr;
import io.zrz.sqldom.model.WindowName;
import io.zrz.sqldom.model.WindowRef;
import io.zrz.sqldom.model.WithClause;
import io.zrz.sqldom.model.SelectSource.Visitor;
import lombok.SneakyThrows;

public class PostgresqlDialect {

  public String write(SelectStatement select) {
    return new Context().visit(select);
  }

  static class Context implements SqlExpr.Visitor<String> {

    Map<SelectSource, String> identifiers = Maps.newHashMap();
    private final AtomicInteger idalloc = new AtomicInteger(0);

    private String identifier(SelectSource source) {
      return ident(this.identifiers.computeIfAbsent(source,
          (s) -> (source.exportedName() == null) ? "source" + this.idalloc.getAndIncrement() : source.exportedName()));
    }

    public String visit(SqlStatement stmt) {
      if (stmt instanceof SelectStatement) {
        return this.visit((SelectStatement) stmt);
      }
      if (stmt instanceof InsertStatement) {
        return this.visit((InsertStatement) stmt);
      }
      throw new IllegalArgumentException(stmt.getClass().toString());
    }

    public String visit(InsertStatement insert) {

      final StringBuilder sb = new StringBuilder();

      if (insert.with() != null) {
        sb.append("WITH ");
        // sb.append(insert.with().stream().map(e ->
        // this.visit(e.statement())).map(in -> String.format("(%s)",
        // in)).collect(Collectors.joining(",\n ")));
        sb.append(insert.with().stream().map(this::visit).collect(Collectors.joining(",\n  ")));
        sb.append(" ");
        sb.append("\n   ");
      }

      sb.append("INSERT INTO ");
      sb.append(this.visitFrom(insert.targetTable()));
      sb.append(" (");
      sb.append(insert.columnNames().stream().collect(Collectors.joining(", ")));
      sb.append(") ");

      final String res = insert.query().apply(new InsertSourceVisitor<String>() {

        @Override
        public String visitSelectStatementInsertSource(SelectStatement stmt) {
          return Context.this.visit(stmt);
        }

        @Override
        public String visitValueListInsertSource(ValueListSource values) {
          final StringBuilder sb = new StringBuilder();
          sb.append("VALUES");
          int loop = 0;
          for (final SqlExpr[] value : values.rows()) {

            if (loop++ > 0) {
              sb.append("\n, ");
            }

            sb.append("(");
            sb.append(Arrays.stream(value).map(f -> Context.this.visit(f)).collect(Collectors.joining(", ")));
            sb.append(")");

          }
          return sb.toString();
        }

      });

      sb.append(res);

      if (insert.onConflict() != null) {

        sb.append(" ON CONFLICT ");

        if (insert.onConflict().target() != null) {
          sb.append(this.visit(insert.onConflict().target()));
          sb.append(" ");
        }

        if (insert.onConflict().action() == null) {
          sb.append("DO NOTHING");
        } else {
          sb.append(this.visit(insert.onConflict().action()));
        }

      }

      if (insert.returning() != null) {
        sb.append(" RETURNING ");
        sb.append(insert.returning().stream().map(f -> this.visit(f)).collect(Collectors.joining(", ")));
      }

      return sb.toString();
    }

    private String visit(ConflictTarget target) {
      final StringBuilder sb = new StringBuilder();
      sb.append("ON CONSTRAINT ");
      final ConstraintConflictTarget ct = (ConstraintConflictTarget) target;
      sb.append(ct.constraintName());
      return sb.toString();
    }

    private String visit(ConflictAction a) {

      final StringBuilder sb = new StringBuilder();

      final UpdateConflictAction action = (UpdateConflictAction) a;

      sb.append("DO UPDATE SET ");

      final String res = action.columns().entrySet().stream()
          .map(col -> String.format("%s = %s", col.getKey(), this.visit(col.getValue())))
          .collect(Collectors.joining(", "));

      sb.append(res);

      if (action.where() != null) {
        sb.append(" WHERE ");
        sb.append(this.visit(action.where()));
      }

      return sb.toString();
    }

    public String visit(SelectStatement select) {

      final StringBuilder sb = new StringBuilder();

      if (select.with() != null) {
        sb.append("WITH ");
        sb.append(select.with().stream().map(this::visit).collect(Collectors.joining(",\n  ")));
        sb.append(" ");
        sb.append("\n   ");
      }

      sb.append("SELECT ");

      if (select.distinct() != null) {
        sb.append("DISTINCT ON (");
        sb.append(this.visit(select.distinct()));
        sb.append(") ");
      }

      sb.append(select.select().stream().map(this::visit).collect(Collectors.joining(", ")));

      if (select.from() != null && !select.from().isEmpty()) {
        sb.append(" FROM ");
        sb.append(select.from().stream().map(this::visitFrom).collect(Collectors.joining(", ")));
      }

      if (select.where() != null) {
        sb.append(" WHERE ");
        sb.append(this.visit(select.where()));
      }

      if (select.groupBy() != null) {
        sb.append(" GROUP BY ");
        sb.append(this.visit(select.groupBy()));
      }

      if (select.window() != null) {
        sb.append(" WINDOW ");
        sb.append(this.visit(select.window()));
      }

      if (select.combine() != null) {
        sb.append(this.visit(select.combine()));
      }

      // if (select.groupBy() != null) {
      // sb.append("[JOIN] ");

      if (select.having() != null) {
        sb.append(" ");
        sb.append(this.visit(select.having()));
      }

      if (select.orderBy() != null) {
        sb.append(" ");
        sb.append(this.visit(select.orderBy()));
      }

      if (select.limit() != null) {
        sb.append(" LIMIT ");
        sb.append(select.limit().count());
      }

      if (select.offset() != null) {
        sb.append(" OFFSET ").append(select.offset());
      }

      if (select.forLock() != null) {
        sb.append(" ");
        sb.append(this.visit(select.forLock()));
      }

      return sb.toString();

    }

    private String visit(CombineClause combine) {
      final StringBuilder sb = new StringBuilder();
      sb.append("\n");
      sb.append(" UNION ALL ");
      sb.append(this.visit(combine.source()));
      return sb.toString();
    }

    private String visit(ForLockClause forLock) {

      final StringBuilder sb = new StringBuilder();

      sb.append("FOR ");

      switch (forLock.mode()) {
        case KEY_SHARE:
          sb.append("KEY SHARE");
          break;
        case NO_KEY_UPDATE:
          sb.append("NO KEY UPDATE");
          break;
        case SHARE:
          sb.append("SHARE");
          break;
        case UPDATE:
          sb.append("UPDATE");
          break;
        default:
          break;

      }

      if (forLock.tablerefs() != null && !forLock.tablerefs().isEmpty()) {
        sb.append(" OF ");
        sb.append(forLock.tablerefs().stream().collect(Collectors.joining(", ")));
      }

      if (forLock.action() != null) {
        switch (forLock.action()) {
          case NOWAIT:
            sb.append(" NOWAIT");
            break;
          case SKIP:
            sb.append(" SKIP");
            break;
          default:
            throw new AssertionError();
        }
      }

      return sb.toString();
    }

    private String visit(OrderByClause orderBy) {
      final StringBuilder sb = new StringBuilder();
      sb.append("ORDER BY ");
      sb.append(orderBy.expressions().stream().map(e -> this.visit(e)).collect(Collectors.joining(", ")));
      return sb.toString();
    }

    private String visit(HavingClause having) {
      final StringBuilder sb = new StringBuilder();
      sb.append("HAVING ");
      throw new AssertionError();
    }

    private String visit(WindowClause window) {
      throw new AssertionError();
    }

    private String visit(GroupByClause groupBy) {
      final StringBuilder sb = new StringBuilder();
      sb.append(this.visit(groupBy.expression()));
      return sb.toString();
    }

    private String visitFrom(SelectSource from) {

      final StringBuilder sb = new StringBuilder();

      sb.append(from.apply(new Visitor<String>() {

        @Override
        public String visitNamedTable(NamedTable source) {
          final StringBuilder sb = new StringBuilder();
          sb.append(ident(source.tableName()));
          if (source.alias() != null) {
            sb.append(" AS ").append(Context.this.identifier(source));
          }
          return sb.toString();
        }

        @Override
        public String visitJoin(JoinSource join) {
          final StringBuilder sb = new StringBuilder();
          sb.append(Context.this.visitFrom(join.source()));
          switch (join.joinType()) {
            case JOIN:
              sb.append(" INNER JOIN ");
              break;
            case LEFT:
              sb.append(" LEFT OUTER JOIN ");
              break;
            case RIGHT:
              sb.append(" RIGHT OUTER JOIN ");
              break;
            case FULL:
              sb.append(" FULL OUTER JOIN ");
              break;
            case CROSS:
              sb.append(" CROSS JOIN ");
              break;
            case NATURAL:
              sb.append(" NATURAL JOIN ");
              break;
            default:
              throw new AssertionError();
          }
          sb.append(Context.this.visitFrom(join.joinWith()));
          sb.append(" ON ");
          sb.append(Context.this.visit(join.using()));
          return sb.toString();
        }

        @Override
        public String visitStatementSource(StatementSource stmt) {
          final StringBuilder sb = new StringBuilder();
          sb.append("(");
          sb.append(Context.this.visit(stmt.statement()));
          sb.append(")");
          sb.append(" AS ").append(Context.this.identifier(stmt));
          // TODO: shape?
          return sb.toString();
        }

        @Override
        public String visitValueListSelectSource(ValueListSource values) {
          final StringBuilder sb = new StringBuilder();
          sb.append("(VALUES ");
          sb.append(values.rows().stream()
              .map(row -> Arrays.stream(row).map(expr -> expr.apply(Context.this)).collect(Collectors.joining(", ", "(", ")")))
              .collect(Collectors.joining(", ")));
          sb.append(")");
          sb.append(" AS ").append(Context.this.identifier(values));
          if (values.shape() != null) {
            sb.append("(");
            sb.append(values.shape().stream().collect(Collectors.joining(", ")));
            sb.append(")");
          }
          return sb.toString();
        }

        @Override
        public String visitFunctionSelectSource(FunctionSelectSource func) {
          final StringBuilder sb = new StringBuilder();
          sb.append(func.functionName()).append("(");
          sb.append(func.functionParameters().stream().map(a -> Context.this.visit(a)).collect(Collectors.joining(", ")));
          sb.append(")");
          sb.append(" AS ").append(Context.this.identifier(func));
          return sb.toString();
        }

      }));
      return sb.toString();

    }

    private String visit(SelectField select) {
      final StringBuilder sb = new StringBuilder();
      sb.append(select.expression().apply(this));
      if (select.alias() != null) {
        sb.append(" AS ");
        sb.append(PostgresqlDialect.ident(select.alias()));
      }
      return sb.toString();
    }

    private String visit(WithClause item) {
      final StringBuilder sb = new StringBuilder();
      sb.append(item.queryName());
      sb.append(" AS (");
      sb.append(this.visit(item.statement()));
      sb.append(")");
      return sb.toString();

    }

    public String visit(SqlExpr expr) {
      return expr.apply((SqlExpr.Visitor<String>) this);
    }

    @Override
    @SneakyThrows
    public String visitStringExpr(StringExpr expr) {
      final StringBuilder sb = new StringBuilder("'");
      Utils.escapeLiteral(sb, expr.value(), true);
      sb.append("'");
      return sb.toString();
    }

    @SneakyThrows
    @Override
    public String visitIdentExpr(IdentExpr expr) {
      if (expr.value().equals("*")) {
        // HACK
        return "*";
      }

      return Utils.escapeIdentifier(null, expr.value()).toString();
    }

    @SneakyThrows
    @Override
    public String visitTableRefExpr(TableRefExpr expr) {
      final StringBuilder sb = new StringBuilder();
      sb.append(this.visit(expr.source()));
      sb.append(".");
      // hack for now
      if (expr.field().equals("*")) {
        sb.append("*");
        return sb.toString();
      }
      return Utils.escapeIdentifier(sb, expr.field()).toString();
    }

    @SneakyThrows
    private String visit(SourceRef source) {

      return Context.this.identifier(source.getSource());

    }

    @Override
    public String visitFunctionExpr(FunctionExpr expr) {
      final StringBuilder sb = new StringBuilder();
      sb.append(expr.function());
      sb.append("(");
      sb.append(expr.parameters().stream().map(this::visit).collect(Collectors.joining(", ")));
      sb.append(")");
      return sb.toString();
    }

    @Override
    public String visitSelectExpr(SelectExpr expr) {
      final StringBuilder sb = new StringBuilder();
      sb.append("(");
      sb.append(this.visit(expr.statement()));
      sb.append(")");
      return sb.toString();
    }

    @Override
    public String visitAggrFuncExpr(AggrFuncExpr expr) {
      final StringBuilder sb = new StringBuilder();
      sb.append(expr.function().function());
      sb.append("(");
      sb.append(expr.function().parameters().stream().map(this::visit).collect(Collectors.joining(", ")));
      sb.append(" ");
      sb.append(this.visit(expr.orderBy()));
      sb.append(")");
      return sb.toString();
    }

    @Override
    public String visitWindowFuncExpr(WindowFuncExpr expr) {
      final StringBuilder sb = new StringBuilder();
      sb.append(expr.function().function());
      sb.append("(");
      sb.append(expr.function().parameters().stream().map(this::visit).collect(Collectors.joining(", ")));
      sb.append(")");
      sb.append(" OVER ");
      sb.append(expr.window().apply(new WindowRef.Visitor<String>() {

        @Override
        public String visitWindowName(WindowName expr) {
          return expr.name();
        }

        @Override
        public String visitWindowDefinition(WindowDefinition expr) {
          final StringBuilder sb = new StringBuilder();
          sb.append("(");

          if (expr.partitionBy() != null) {
            sb.append("PARTITION BY ");
            sb.append(Context.this.visit(expr.partitionBy()));
          }

          if (expr.orderBy() != null) {
            sb.append(" ");
            sb.append(Context.this.visit(expr.orderBy()));
          }

          sb.append(")");
          return sb.toString();
        }

      }));

      return sb.toString();
    }

    @Override
    public String visitCastExpr(CastExpr expr) {
      final StringBuilder sb = new StringBuilder();
      sb.append("CAST(");
      sb.append(this.visit(expr.expression()));
      sb.append(" AS ");
      sb.append(expr.type());
      sb.append(")");
      return sb.toString();
    }

    @Override
    public String visitBinaryExpr(BinaryExpr expr) {
      final StringBuilder sb = new StringBuilder();
      sb.append("(");
      sb.append(this.visit(expr.left()));
      sb.append(" ").append(expr.operator()).append(" ");
      sb.append(this.visit(expr.right()));
      sb.append(")");
      return sb.toString();
    }

    @Override
    public String visitIntExpr(IntExpr expr) {
      return Integer.toString(expr.value());
    }

    @Override
    public String visitBoolExpr(BoolExpr expr) {
      return Boolean.toString(expr.value());
    }

    @Override
    public String visitParamExpr(ParamExpr expr) {
      if (expr.name() == null) {
        return "?";
      }
      return expr.name();
    }

    @Override
    public String visitRawExpr(RawExpr rawExpr) {
      return rawExpr.value();
    }

    @Override
    public String visitNullExpr(NullExpr expr) {
      return "NULL";
    }

    @Override
    public String visitArrayFuncExpr(ArrayExpr arr) {
      final StringBuilder sb = new StringBuilder();
      sb.append("ARRAY[");
      sb.append(arr.values().stream().map(val -> val.apply(this)).collect(Collectors.joining(", ")));
      sb.append("]");
      return sb.toString();
    }

    @Override
    public String visitSelectSourceExpr(SelectSource expr) {
      return this.identifier(expr);
    }

  }

  public static String generate(SelectStatement select) {
    return new Context().visit(select);
  }

  public static String generate(InsertStatement insert) {
    return new Context().visit(insert);
  }

  @SneakyThrows
  private static String ident(String ident) {
    return Utils.escapeIdentifier(null, ident).toString();
  }

}
