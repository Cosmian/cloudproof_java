# -*- coding: utf-8 -*-
import shutil
import urllib.request
import zipfile
from os import getenv, path, remove


def files_to_be_copied(name: str):
    destination = 'src/main/resources'
    return {
        f'tmp/x86_64-apple-darwin/x86_64-apple-darwin/release/libcloudproof_{name}.dylib': f'{destination}/darwin-x86-64/libcloudproof_{name}.dylib',
        f'tmp/x86_64-unknown-linux-gnu/x86_64-unknown-linux-gnu/release/libcloudproof_{name}.so': f'{destination}/linux-x86-64/libcloudproof_{name}.so',
        f'tmp/x86_64-pc-windows-gnu/x86_64-pc-windows-gnu/release/cloudproof_{name}.dll': f'{destination}/win32-x86-64/cloudproof_{name}.dll',
    }


def download_native_libraries(name: str, version: str) -> bool:
    to_be_copied = files_to_be_copied('findex')
    cover_crypt_files = files_to_be_copied('cover_crypt')
    to_be_copied.update(cover_crypt_files)

    missing_files = False
    for key in to_be_copied:
        if not path.exists(to_be_copied[key]):
            missing_files = True
            break

    if missing_files:
        url = f'https://package.cosmian.com/{name}/{version}/all.zip'
        try:
            r = urllib.request.urlopen(url)
            if r.getcode() != 200:
                print(f'Cannot get {name} {version} (status code: {r.getcode()})')
            else:
                if path.exists('tmp'):
                    shutil.rmtree('tmp')
                if path.exists('all.zip'):
                    remove('all.zip')

                open('all.zip', 'wb').write(r.read())
                with zipfile.ZipFile('all.zip', 'r') as zip_ref:
                    zip_ref.extractall('tmp')
                    for key in to_be_copied:
                        shutil.copyfile(key, to_be_copied[key])
                        print(f'Copied OK: {to_be_copied[key]}...')

                    shutil.rmtree('tmp')
                remove('all.zip')
        except Exception as e:
            print(f'Cannot get {name} {version} ({e})')
            return False
    return True


if __name__ == '__main__':
    ret = download_native_libraries('cloudproof_rust', 'v1.0.0')
    if ret is False and getenv('GITHUB_ACTIONS'):
        download_native_libraries('cloudproof_rust', 'last_build')
