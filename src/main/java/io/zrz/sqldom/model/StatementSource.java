package io.zrz.sqldom.model;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

import io.zrz.hai.sql.model.ImmutableStatementSource;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC)
@Value.Modifiable
public interface StatementSource extends SelectSource {

  SqlStatement statement();

  /**
   * required name
   */

  String alias();

  @Override
  default String exportedName() {
    return alias();
  }

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitStatementSource(this);
  }

  static SelectSource of(SqlStatement stmt, String alias) {
    return ImmutableStatementSource.builder().statement(stmt).alias(alias).build();
  }

}
