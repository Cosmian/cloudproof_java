# -*- coding: utf-8 -*-
import shutil
import urllib.request
import zipfile
from os import getenv, path, remove


def files_to_be_copied(name: str):
    """
    Returns the list of files to be copied
    """
    destination = 'src/main/resources'
    return {
        f'tmp/x86_64-apple-darwin/x86_64-apple-darwin/release/libcosmian_{name}.dylib': f'{destination}/darwin-x86-64/libcosmian_{name}.dylib',
        f'tmp/x86_64-unknown-linux-gnu/x86_64-unknown-linux-gnu/release/libcosmian_{name}.so': f'{destination}/linux-x86-64/libcosmian_{name}.so',
        f'tmp/x86_64-pc-windows-gnu/x86_64-pc-windows-gnu/release/cosmian_{name}.dll': f'{destination}/win32-x86-64/cosmian_{name}.dll',
    }


def download_native_libraries(name: str, version: str) -> bool:
    """Download and extract native libraries"""
    to_be_copied = files_to_be_copied(name)

    missing_files = True
    for key, value in to_be_copied.items():
        if not path.exists(value):
            missing_files = True
            break

    if missing_files:
        url = f'https://package.cosmian.com/{name}/{version}/all.zip'
        try:
            with urllib.request.urlopen(url) as request:
                if request.getcode() != 200:
                    print(
                        f'Cannot get {name} {version} \
                            (status code: {request.getcode()})'
                    )
                else:
                    if path.exists('tmp'):
                        shutil.rmtree('tmp')
                    if path.exists('all.zip'):
                        remove('all.zip')

                    # pylint: disable=consider-using-with
                    open('all.zip', 'wb').write(request.read())

                    with zipfile.ZipFile('all.zip', 'r') as zip_ref:
                        zip_ref.extractall('tmp')
                        for key, value in to_be_copied.items():
                            shutil.copyfile(key, value)
                            print(f'Copied OK: {value}...')

                        shutil.rmtree('tmp')
                    remove('all.zip')
        # pylint: disable=broad-except
        except Exception as exception:
            print(f'Cannot get {name} {version} ({exception})')
            return False
    return True


if __name__ == '__main__':
    ret = download_native_libraries('findex', 'v2.0.3')
    if ret is False and getenv('GITHUB_ACTIONS'):
        download_native_libraries('findex', 'last_build')
    ret = download_native_libraries('cover_crypt', 'v8.0.2')
    if ret is False and getenv('GITHUB_ACTIONS'):
        download_native_libraries('cover_crypt', 'last_build')
