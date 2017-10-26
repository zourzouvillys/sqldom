package io.zrz.sqldom.model;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, depluralize = true)
@Value.Modifiable
public interface AggrFuncExpr extends SqlExpr {

  FunctionExpr function();

  OrderByClause orderBy();

  SqlExpr filter();

  boolean withinGroup();

  @Override
  default <R> R apply(Visitor<R> visitor) {
    return visitor.visitAggrFuncExpr(this);
  }

}
