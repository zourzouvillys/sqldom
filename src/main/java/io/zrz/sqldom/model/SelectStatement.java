package io.zrz.sqldom.model;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

import io.zrz.hai.sql.model.ImmutableSelectExpr;
import io.zrz.sqldom.SqlModel;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, from = "toImmutable")
@Value.Modifiable
public interface SelectStatement extends SqlStatement, InsertSource {

  @Nullable
  SqlExpr distinct();

  @Nullable
  List<WithClause> with();

  List<SelectField> select();

  List<SelectSource> from();

  @Nullable
  SqlExpr where();

  @Nullable
  GroupByClause groupBy();

  @Nullable
  CombineClause combine();

  @Nullable
  WindowClause window();

  @Nullable
  HavingClause having();

  @Nullable
  OrderByClause orderBy();

  @Nullable
  LimitClause limit();

  @Nullable
  Integer offset();

  @Nullable
  ForLockClause forLock();

  default SelectExpr toExpr() {
    return ImmutableSelectExpr.builder().statement(this).build();
  }

  default SelectSource toSource(String alias) {
    return SqlModel.source(this).setAlias(alias).toImmutable();
  }

  @Override
  default <R> R apply(InsertSourceVisitor<R> visitor) {
    return visitor.visitSelectStatementInsertSource(this);
  }

}
