package io.zrz.sqldom.model;

import lombok.Value;

/**
 * a reference to a named source in the local scope.
 */

@Value(staticConstructor = "of")
public class SourceRef implements SqlExpr {

  private final SelectSource source;

  @Override
  public <R> R apply(Visitor<R> visitor) {
    return visitor.visitSelectSourceExpr(this.source);
  }

}
