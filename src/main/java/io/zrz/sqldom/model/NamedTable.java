package io.zrz.sqldom.model;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

import io.zrz.sqldom.SqlModel;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC)
@Value.Modifiable
public interface NamedTable extends SelectSource {

  String tableName();

  @Nullable
  String alias();

  @Override
  default String exportedName() {
    if (alias() == null) {
      return tableName();
    }
    return alias();
  }

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitNamedTable(this);
  }

  static NamedTable of(String tableName) {
    return SqlModel.namedTable(tableName);
  }

  static NamedTable of(String tableName, String alias) {
    return SqlModel.namedTable(tableName, alias);
  }

}
