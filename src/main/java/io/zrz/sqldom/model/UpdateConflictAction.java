package io.zrz.sqldom.model;

import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, from = "toImmutable")
@Value.Modifiable
public interface UpdateConflictAction extends ConflictAction {

  Map<String, SqlExpr> columns();

  @Nullable
  SqlExpr where();

}
