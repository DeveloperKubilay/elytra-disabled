
  ![plugin-fon](https://github.com/user-attachments/assets/5aa6f542-ebd8-4ba2-82da-d542b66d6ce6)

**ElytraDisabled** - A comprehensive solution for controlling elytra usage on your server. The plugin completely disables elytra in specified worlds, providing maximum protection against bypasses.

https://modrinth.com/plugin/elytra-disabled

**Features**:

- Complete blocking of equipping and flying with elytra
- Auto-removal when entering restricted worlds
- Advanced anti-bypass protection with tick-by-tick monitoring
- Smart cooldown-based notifications to prevent spam
- Multi-language support (English, Russian, Ukrainian)
- Configurable sound effects for blocked actions
- Automatic update checking via Modrinth API
- Protection against dispenser armor equip exploits
- Optimized performance with efficient cooldown management

Perfect for servers that value fair gameplay, stability, and absolute control over elytra mechanics. ElytraDisabled ensures predictable player behavior and eliminates any possibility of unwanted flight exploits.
## config.yml

```
# ===================================================
# ElytraDisabled - Основные настройки / Main Settings
# ===================================================

settings:
  # Включить плагин? / Enable plugin?
  enable_plugin: true

  # Проверять наличие обновлений плагина при запуске
  # Check for plugin updates on startup
  check_updates: true

  # Язык плагина (ru, en, ua) / Plugin language (ru, en, ua)
  # ru - русский, en - english, ua - українська
  language: en

  # Кулдаун между сообщениями игроку (в секундах), чтобы не спамить при повторных попытках
  # Cooldown between messages to player (in seconds), to avoid spam on repeated attempts
  message_cooldown: 3

  # Список миров, где запрещены элитры
  # List of worlds where elytra is disabled
  disable_in_worlds:
    - world_the_end

  # Запретить надевание элитр в запрещённых мирах
  # Если false - игроки смогут носить элитры, но не смогут летать
  # ВАЖНО: Для работы при false необходимо также установить force_unequip_on_enter: false
  #
  # Prevent equipping elytra in disabled worlds
  # If false - players can wear elytra but cannot glide
  # IMPORTANT: For this to work when false, you must also set force_unequip_on_enter: false
  prevent_equip: true

  # Автоматически снимать элитры при входе в запрещённый мир
  # Automatically remove elytra when entering disabled world
  force_unequip_on_enter: true

  # Останавливать активный полёт на элитрах в запрещённых мирах
  # Stop active elytra flight in disabled worlds
  stop_existing_glide: true

  # Дополнительная подстраховка на редкие случаи обхода. Основная защита не нагружает
  # сервер и работает мгновенно. В тиках (20 = 1 секунда). 0 - отключить.
  # Extra safety net for rare bypass cases. Main protection is instant and has no
  # performance cost. In ticks (20 = 1 second). 0 to disable.
  safety_net_interval_ticks: 100

  # Способ показа сообщений (чат / текст над инвентарём / по центру экрана) и звук
  # настраиваются не здесь, а прямо в файлах lang/<язык>.yml - у каждого сообщения свои.
  # Смотрите комментарии в начале lang/ru.yml.
  #
  # How messages are shown (chat / text above the inventory / center of the screen) and
  # their sound are configured per-message in lang/<language>.yml, not here.
  # See the comments at the top of lang/en.yml.

# ===================================================================
# PlaceholderAPI (если установлен на сервере) / PlaceholderAPI (if installed)
# ===================================================================
# %elytradisabled_world_disabled% - запрещены ли элитры в текущем мире игрока
# %elytradisabled_bypass%         - есть ли у игрока право обхода (bypass)
# %elytradisabled_blocked%        - реально ли блокируется прямо сейчас (мир запрещён И нет bypass)
# Текст для true/false настраивается в lang/<язык>.yml: placeholder_true / placeholder_false
#
# %elytradisabled_world_disabled% - whether elytra is disabled in the player's current world
# %elytradisabled_bypass%         - whether the player has the bypass permission
# %elytradisabled_blocked%        - whether it's actually being blocked right now (world
#                                    disabled AND no bypass)
# The true/false text is configured in lang/<language>.yml: placeholder_true / placeholder_false

permissions:
  # Игроки с этим пермишеном могут использовать элитры в любых мирах
  # Players with this permission can use elytra in any worlds
  bypass: "elytradisabled.bypass"
```
# Video
[![Video](https://img.youtube.com/vi/0RfcnuwPMrI/hqdefault.jpg)](https://www.youtube.com/embed/0RfcnuwPMrI)
