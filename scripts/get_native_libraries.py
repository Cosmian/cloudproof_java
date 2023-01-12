# -*- coding: utf-8 -*-
import requests
import shutil
import zipfile

from os import path, remove


def download_native_libraries(name: str, version: str, destination: str):
    mac = f'{destination}/darwin-x86-64/libcosmian_{name}.dylib'
    linux = f'{destination}/linux-x86-64/libcosmian_{name}.so'
    windows = f'{destination}/win32-x86-64/cosmian_{name}.dll'

    if (
        not path.exists(mac)
        or not path.exists(linux)
        or not path.exists(windows)
    ):
        print(
            f'Missing {name} native library. Copy {name} {version} to {destination}...'
        )

        url = f'https://package.cosmian.com/{name}/{version}/all.zip'
        r = requests.get(url, allow_redirects=True)
        if r.status_code != 200:
            download_native_libraries(name, 'last_build', destination)
        else:
            if path.exists('tmp'):
                shutil.rmtree('tmp')
            if path.exists('all.zip'):
                remove('all.zip')

            open('all.zip', 'wb').write(r.content)
            with zipfile.ZipFile('all.zip', 'r') as zip_ref:
                zip_ref.extractall('tmp')
                shutil.copyfile(
                    f'tmp/x86_64-apple-darwin/x86_64-apple-darwin/release/libcosmian_{name}.dylib',
                    f'{mac}',
                )
                shutil.copyfile(
                    f'tmp/x86_64-unknown-linux-gnu/x86_64-unknown-linux-gnu/release/libcosmian_{name}.so',
                    f'{linux}',
                )
                shutil.copyfile(
                    f'tmp/x86_64-pc-windows-gnu/x86_64-pc-windows-gnu/release/cosmian_{name}.dll',
                    f'{windows}',
                )
                shutil.rmtree('tmp')
            remove('all.zip')


if __name__ == '__main__':
    download_native_libraries('findex', 'v2.0.0', 'src/main/resources')
    download_native_libraries('cover_crypt', 'v8.0.2', 'src/main/resources')
