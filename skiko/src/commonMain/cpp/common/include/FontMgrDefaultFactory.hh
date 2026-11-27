#ifndef FONT_MGR_DEFAULT_FACTORY_H
#define FONT_MGR_DEFAULT_FACTORY_H

#include "SkFontMgr.h"

// Lazily creates one default manager shared by all callers on every target.
// Initialization is thread-safe; the manager retains a process-lifetime reference.
// On Linux, creating a new Shaper does not reload fonts or Fontconfig configuration.
// The platform factory's null result, if any, is also cached.
sk_sp<SkFontMgr> SkFontMgrSkikoDefault();

#endif
