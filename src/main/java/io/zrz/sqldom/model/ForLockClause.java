package io.zrz.sqldom.model;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, from = "toImmutable")
@Value.Modifiable
public interface ForLockClause {

  enum Mode {
    UPDATE, NO_KEY_UPDATE, SHARE, KEY_SHARE
  }

  enum ReadAction {
    NOWAIT, SKIP
  }

  Mode mode();

  List<String> tablerefs();

  @Nullable
  ReadAction action();

}
