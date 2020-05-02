# bluetooth-tile
Reverse engineering a bluetooth tile from [Aliexpress](https://www.aliexpress.com/item/1pc-Brand-New-Smart-Tag-Wireless-Bluetooth-Tracker-Child-Kid-Bag-Luggage-Key-Finder-itag-Wallet/32727586658.html?tt=Copy+to+clipboard&fbclid=IwAR3OYJ2GhDCZ9wCLB52M-qb8nl7B25SBGDDr00iDYlvwbwEg4IVGICzV6Bk&aff_platform=default&cpt=1545295423135&sk=WNT7S1i&aff_trace_key=697f75e6ecec4f23aea518e724cf23d5-1545295423135-02062-WNT7S1i&terminal_id=e27c67f8b6cc4197acf927a2fe0f3e42)

## Why?
The app that works with the tile was pretty bad ([link](https://play.google.com/store/apps/details?id=com.sl.fdq.activity&hl=en)) and requested unnecessary permissions so I tried to make a better version.

## Permissions
As declared in the manifest, three permissions are required.
- android.permission.BLUETOOTH
- android.permission.BLUETOOTH_ADMIN
- android.permission.ACCESS_COARSE_LOCATION

For why ACCESS_COARSE_LOCATION is required, see 
- https://issuetracker.google.com/issues/37065090
- https://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
