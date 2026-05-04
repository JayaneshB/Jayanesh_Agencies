import { DotLottieReact } from '@lottiefiles/dotlottie-react';

export default function Loader() {
  return (
    <div className="flex items-center justify-center w-full min-h-[60vh]">
      <DotLottieReact
        src="/loader.lottie"
        loop
        autoplay
        style={{ width: 180, height: 180 }}
      />
    </div>
  );
}
