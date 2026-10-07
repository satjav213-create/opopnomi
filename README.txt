OP OP NO MI - Mod Forge 1.16.5
==============================

Isi: item "Gomu Gomu no Mi" (buah iblis manusia karet) dengan 3 skill.

CARA MEMBUAT .JAR (GitHub Actions, tanpa memasang apa pun)
-----------------------------------------------------------
1. Buat repository baru di github.com.
2. Add file > Upload files, seret SEMUA isi folder ini (termasuk folder .github).
   Kalau folder .github tidak ikut terunggah: Add file > Create new file, ketik nama
   ".github/workflows/build.yml" lalu tempel isi file workflow-build.yml.
3. Klik Commit changes. Buka tab Actions, tunggu build selesai (beberapa menit).
4. Klik hasil build > bagian Artifacts > unduh "opopnomi-jar".
5. Ekstrak zip-nya, taruh opopnomi-1.0.0.jar di folder mods Minecraft Forge 1.16.5
   (Forge 36.2.39 atau lebih baru). Mod harus dipasang juga di server jika main multiplayer.

CARA PAKAI DI GAME
------------------
- Ambil buah: tab kreatif "Op Op no Mi", atau perintah: /give @s opopnomi:gomu_gomu_no_mi
- Makan buahnya. Petunjuk skill muncul di layar kiri bawah.
- R  = ganti skill
- G  = pakai skill (tombol bisa diubah di Options > Controls > Op Op no Mi)
- /removedf = menghilangkan kekuatan buah iblis

SKILL
-----
1. Gomu Gomu no Pistol : kedua tangan memanjang ke depan, mob yang kena terkena hit.
2. Gomu Gomu no Ketapel: tangan memanjang ke belakang 5 detik, lalu dilontarkan ke depan.
                         Mob yang kena terkena hit dan terpental.
3. Gomu Gomu no Gatling: (ULTIMATE) tangan jadi banyak seperti ilusi, memukul bertubi-tubi
                         selama 10 detik. Mob yang kena sakit dan terkena stun 5 detik.

MENYESUAIKAN KEKUATAN
---------------------
Buka src/main/java/com/opopnomi/mod/Skills.java : damage, jangkauan, cooldown, durasi.
