package io.zrz.sqldom.model;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, from = "toImmutable")
@Value.Modifiable
public interface InsertStatement extends SqlStatement {

  @Nullable
  List<WithClause> with();

  NamedTable targetTable();

  List<String> columnNames();

  InsertSource query();

  @Nullable
  ConflictClause onConflict();

  @Nullable
  List<SqlExpr> returning();

}
