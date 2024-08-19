import yt_dlp
import os
import traceback

def download_video(url, output_dir):
    try:
        # Ensure the output directory exists
        if not os.path.exists(output_dir):
            os.makedirs(output_dir)

        ydl_opts = {
            'outtmpl': os.path.join(output_dir, '%(title)s.%(ext)s'),
            'format': 'best',
        }
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info_dict = ydl.extract_info(url, download=True)
            return ydl.prepare_filename(info_dict)
    except yt_dlp.utils.DownloadError as e:
        return f"DownloadError: {str(e)}"
    except Exception as e:
        traceback.print_exc()  # Print stack trace for debugging
        return f"An error occurred: {str(e)}"
