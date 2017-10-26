package io.zrz.sqldom.model;

import io.zrz.hai.sql.model.ImmutableJoinSource;
import io.zrz.hai.sql.model.ImmutableTableRefExpr;

public interface SelectSource {

  String exportedName();

  interface Visitor<R> {

    R visitNamedTable(NamedTable source);

    R visitJoin(JoinSource join);

    R visitStatementSource(StatementSource stmt);

    R visitValueListSelectSource(ValueListSource values);

    R visitFunctionSelectSource(FunctionSelectSource values);

  }

  <R> R apply(SelectSource.Visitor<R> context);

  default JoinSource innerJoin(SelectSource source, SqlExpr expr) {
    return ImmutableJoinSource.builder().source(this).joinType(JoinType.JOIN).joinWith(source).using(expr).build();
  }

  default JoinSource join(JoinType type, SelectSource source, SqlExpr expr) {
    return ImmutableJoinSource.builder().source(this).joinType(type).joinWith(source).using(expr).build();
  }

  default TableRefExpr ref(String field) {
    return ImmutableTableRefExpr.builder().source(SourceRef.of(this)).field(field).build();
  }

  default TableRefExpr all() {
    return ImmutableTableRefExpr.builder().source(SourceRef.of(this)).field("*").build();
  }

  default NamedTable alias(String alias) {

    return NamedTable.of(alias);

  }

  default SqlExpr toExpr() {
    return SourceRef.of(this);
  }

}
