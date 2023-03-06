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
        f'tmp/x86_64-apple-darwin/x86_64-apple-darwin/release/libcloudproof_{name}.dylib': f'{destination}/darwin-x86-64/libcloudproof_{name}.dylib',
        f'tmp/x86_64-unknown-linux-gnu/x86_64-unknown-linux-gnu/release/libcloudproof_{name}.so': f'{destination}/linux-x86-64/libcloudproof_{name}.so',
        f'tmp/x86_64-pc-windows-gnu/x86_64-pc-windows-gnu/release/cloudproof_{name}.dll': f'{destination}/win32-x86-64/cloudproof_{name}.dll',
    }


def download_native_libraries(version: str) -> bool:
    """Download and extract native libraries"""
    to_be_copied = files_to_be_copied('findex')
    cover_crypt_files = files_to_be_copied('cover_crypt')
    to_be_copied.update(cover_crypt_files)

    missing_files = False
    for key, value in to_be_copied.items():
        if not path.exists(value):
            missing_files = True
            break

    if missing_files:
        url = f'https://package.cosmian.com/cloudproof_rust/{version}/all.zip'
        try:
            with urllib.request.urlopen(url) as request:
                if request.getcode() != 200:
                    print(
                        f'Cannot get cloudproof_rust {version} \
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
            print(f'Cannot get cloudproof_rust {version} ({exception})')
            return False
    return True


if __name__ == '__main__':
    ret = download_native_libraries('v1.0.0')
    if ret is False and getenv('GITHUB_ACTIONS'):
        download_native_libraries('last_build/feature/add_findex')
