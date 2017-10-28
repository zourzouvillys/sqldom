package io.zrz.sqldom.model;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, depluralize = true)
@Value.Modifiable
public interface SelectExpr extends SqlExpr {

  SqlStatement statement();

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitSelectExpr(this);
  }

  static SqlExpr of(SqlStatement statement) {
    return ImmutableSelectExpr.builder().statement(statement).build();
  }

}
