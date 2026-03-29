# Third-Party Runtime Jars

This directory contains the runtime jars used by the core Eclipse plugin.

- `Saxon-HE-12.8.jar`
- `xmlresolver-5.3.3.jar`
- `xmlresolver-5.3.3-data.jar`

If you need to refresh or repopulate these files from Maven artifacts, run:

```bash
scripts/fetch-third-party-libs.sh
```

See the top-level `THIRD-PARTY.md` file for license and redistribution notes.
The built core plug-in also packages:

- `about.html`
- `legal/MPL-2.0.txt`
- `legal/Apache-2.0.txt`
- `legal/THIRD-PARTY-NOTICES.txt`
